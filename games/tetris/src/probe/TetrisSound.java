package probe;
import dev.java2gb.api.*;

import static probe.TetrisHardware.*;
import static probe.TetrisLayout.BYTE_MASK;

public final class TetrisSound {
  public static final int EFFECT_NONE = 0, EFFECT_MOVE = 1, EFFECT_ROTATE = 2;
  public static final int EFFECT_LOCK = 3, EFFECT_CLEAR = 4, EFFECT_TETRIS = 5;
  public static final int EFFECT_SETTLE = 6, EFFECT_OVER = 9, EFFECT_WIN = 10;
  public static final int MUSIC_OFF = 3, SONG_SILENT = 0, SONG_CLEAR = 2;
  public static final int SONG_TITLE = 3, SONG_OVER = 4, FIRST_GAME_SONG = 5;
  private static final int PULSE_ONE = 0, PULSE_TWO = 1, WAVE = 2, NOISE = 3;
  private static final int NOTE_REST = 1, NOTE_END = 256, SECTION_REPEAT = 0xffff;
  private static final int COMMAND_INSTRUMENT = 0x9d, COMMAND_LENGTH = 0xa0;
  private static final int COMMAND_PREFIX_MASK = 0xf0, NIBBLE_MASK = 0x0f;
  private static final int MAX_NOTE_COMMANDS = 32, WORD_BYTES = 2;
  private static final int VIBRATO_DATA = 0x6dcb, FREQUENCY_DATA = 0x6e02, NOISE_DATA = 0x6e94;
  private static final int VIBRATO_PHASE_MASK = 3, VIBRATO_AGE_MASK = 63;
  private static final int SONG_LENGTHS_OFFSET = 1, SONG_CHANNELS_OFFSET = 3;
  private static final int ENVELOPE_OFFSET = 1, FREQUENCY_OFFSET = 2, TRIGGER_OFFSET = 3;
  private static final int SHORT_EFFECT_FRAMES = 5, CLEAR_EFFECT_FRAMES = 18;
  private static final int EFFECT_DUTY = 0xb6, EFFECT_ENVELOPE = 0xa1;
  private static final int NOISE_LENGTH = 0x3b, SETTLE_ENVELOPE = 0x81;
  private static final int LOCK_POLYNOMIAL = 0x40, SETTLE_POLYNOMIAL = 0x51;
  private static final int MOVE_FREQUENCY = 0x740, ROTATE_FREQUENCY = 0x780;
  private static final int TETRIS_FREQUENCY = 0x6d6, DEFAULT_EFFECT_FREQUENCY = 0x783;

  public static int selectionSong(final int selection) {
    return selection == MUSIC_OFF ? SONG_SILENT : selection + FIRST_GAME_SONG;
  }

  private static int channelRegister(final int index) {
    return index == PULSE_ONE ? NR11 : index == PULSE_TWO ? NR21 : NR31;
  }

  private static int effectFrequency(final int id) {
    return id == EFFECT_MOVE ? MOVE_FREQUENCY : id == EFFECT_ROTATE ? ROTATE_FREQUENCY
        : id == EFFECT_TETRIS ? TETRIS_FREQUENCY : DEFAULT_EFFECT_FREQUENCY;
  }

  private static int channelPointer(final int songPointer, final int index) {
    return word(songPointer + SONG_CHANNELS_OFFSET + index * WORD_BYTES);
  }

  public static final class Channel {
    char sequence, pointer, timer, length, frequency;
    char nextNote, nextLength, nextFrequency, nextWave;
    byte envelope, duty, age, nextEnvelope, nextDuty;
    boolean rest, prepared;
  }

  final Channel one = new Channel(),
      two = new Channel(),
      three = new Channel(),
      four = new Channel();
  byte song, effectTimer, noiseTimer;
  char lengths;
  byte vibratoPhase;
  public int notes;
  public byte events;

  static int data(final int address) {
    return TetrisHardware.audioData(address);
  }

  static int word(final int address) {
    return TetrisHardware.audioWord(address);
  }

  static void write(final int register, final int value) {
    TetrisHardware.audio(register, value);
  }

  static void mute() {
    write(NR12, 0);
    write(NR22, 0);
    write(NR30, 0);
    write(NR42, 0);
  }

  @NoInline
  void init(final Channel c, final int pointer) {
    c.sequence = (char) pointer;
    c.pointer = (char) (pointer == 0 ? 0 : word(pointer));
    c.timer = 1;
    c.length = 1;
    c.envelope = 0;
    c.duty = 0;
    c.rest = true;
    c.age = 0;
    c.prepared = false;
  }

  @NoInline
  public void play(final int id) {
    write(NR52, AUDIO_ENABLED);
    write(NR50, AUDIO_MAX_VOLUME);
    write(NR51, AUDIO_ROUTE_ALL);
    write(NR10, 0);
    mute();
    song = (byte) id;
    effectTimer = 0;
    noiseTimer = 0;
    if (id == EFFECT_NONE) {
      return;
    }
    final int pointer = TetrisData.song(id);
    lengths = (char) word(pointer + SONG_LENGTHS_OFFSET);
    init(one, channelPointer(pointer, 0));
    init(two, channelPointer(pointer, 1));
    init(three, channelPointer(pointer, 2));
    init(four, channelPointer(pointer, 3));
    prepare(one, 0);
    prepare(two, 1);
    prepare(three, 2);
    prepare(four, 3);
  }

  @NoInline
  void trigger(final Channel c, final int index, final int note) {
    events++;
    if (index == NOISE) {
      if (noiseTimer == 0) {
        write(NR41, c.duty);
        write(NR42, c.envelope);
        write(NR43, c.frequency & BYTE_MASK);
        write(NR44, (c.frequency >>> 8) | NOTE_TRIGGER);
      }
      return;
    }
    c.rest = note == NOTE_REST;
    c.age = 0;
    if (index == PULSE_ONE && effectTimer != 0) {
      return;
    }
    final int base = channelRegister(index);
    if (index == WAVE) {
      write(NR30, 0);
      write(NR30, AUDIO_ENABLED);
    }
    final int envelope = c.rest ? 1 : index == WAVE ? 0x20 : c.envelope;
    write(base, c.duty);
    write(base + ENVELOPE_OFFSET, envelope);
    write(base + FREQUENCY_OFFSET, c.frequency & BYTE_MASK);
    write(base + TRIGGER_OFFSET, (c.frequency >>> 8) | NOTE_TRIGGER);
    notes++;
  }

  @NoInline
  void step(final Channel c, final int index) {
    if (c.sequence == 0 || song == 0) {
      return;
    }
    if (--c.timer != 0) {
      if (!c.rest && (index != PULSE_ONE || effectTimer == 0)) {
        if (index == WAVE && (c.duty & 128) != 0 && c.timer == 6) {
          write(NR32, 0x40);
        }
        if (index < NOISE && (c.duty & NIBBLE_MASK) != 0 && vibratoPhase == 0) {
          final int packed = data(VIBRATO_DATA + (c.age >>> 1));
          int offset = (c.age & 1) == 0 ? packed >>> 4 : packed & NIBBLE_MASK;
          if (offset >= 8) {
            offset -= 16;
          }
          final int frequency = c.frequency + offset;
          final int reg = channelRegister(index) + FREQUENCY_OFFSET;
          write(reg, frequency & BYTE_MASK);
          write(reg + 1, frequency >>> 8);
        }
      }
      c.age = (byte) ((c.age + 1) & VIBRATO_AGE_MASK);
      return;
    }
    if (!c.prepared) {
      prepare(c, index);
    }
    if (c.nextNote == NOTE_END) {
      song = 0;
      mute();
      return;
    }
    c.length = c.nextLength;
    c.envelope = c.nextEnvelope;
    c.duty = c.nextDuty;
    c.frequency = c.nextFrequency;
    if (index == WAVE && c.nextWave != 0) {
      write(NR30, 0);
      for (int i = 0; i < WAVE_BYTES; i++) {
        write(WAVE_RAM + i, data(c.nextWave + i));
      }
    }
    trigger(c, index, c.nextNote);
    c.prepared = false;
    c.timer = c.length == 0 ? NOTE_END : c.length;
  }

  @NoInline
  void prepare(final Channel c, final int index) {
    if (c.sequence == 0) {
      return;
    }
    c.nextLength = c.length;
    c.nextEnvelope = c.envelope;
    c.nextDuty = c.duty;
    c.nextFrequency = c.frequency;
    c.nextWave = 0;
    c.prepared = true;
    for (int commands = 0; commands < MAX_NOTE_COMMANDS; commands++) {
      final int command = data(c.pointer++);
      if (command == 0) {
        c.sequence += WORD_BYTES;
        int section = word(c.sequence);
        if (section == 0) {
          c.nextNote = NOTE_END;
          return;
        }
        if (section == SECTION_REPEAT) {
          c.sequence = (char) word(c.sequence + WORD_BYTES);
          section = word(c.sequence);
        }
        c.pointer = (char) section;
      } else if (command == COMMAND_INSTRUMENT) {
        c.nextEnvelope = (byte) data(c.pointer++);
        final int parameter = data(c.pointer++);
        c.nextDuty = (byte) data(c.pointer++);
        if (index == WAVE) {
          c.nextWave = (char) ((c.nextEnvelope & BYTE_MASK) | (parameter << 8));
        }
      } else if ((command & COMMAND_PREFIX_MASK) == COMMAND_LENGTH) {
        c.nextLength = (char) data(lengths + (command & NIBBLE_MASK));
      } else {
        c.nextNote = (char) command;
        if (index == NOISE) {
          final int at = NOISE_DATA + command;
          c.nextEnvelope = (byte) data(at);
          c.nextDuty = (byte) data(at + 2);
          c.nextFrequency = (char) word(at + 3);
        } else if (command != NOTE_REST) {
          c.nextFrequency = (char) word(FREQUENCY_DATA + command);
        }
        return;
      }
    }
    c.nextNote = NOTE_END;
  }

  @NoInline
  void prepareNext(final boolean busy) {
    Channel chosen = null;
    int index = 0, deadline = 65536;
    if (one.sequence != 0 && !one.prepared) {
      chosen = one;
      deadline = one.timer;
    }
    if (two.sequence != 0 && !two.prepared && two.timer < deadline) {
      chosen = two;
      index = 1;
      deadline = two.timer;
    }
    if (three.sequence != 0 && !three.prepared && three.timer < deadline) {
      chosen = three;
      index = 2;
      deadline = three.timer;
    }
    if (four.sequence != 0 && !four.prepared && four.timer < deadline) {
      chosen = four;
      index = 3;
      deadline = four.timer;
    }
    if (chosen != null && (!busy || deadline <= 2)) {
      prepare(chosen, index);
    }
  }

  @NoInline
  public void effect(final int id) {
    if (id == EFFECT_NONE) {
      return;
    }
    if (id == EFFECT_LOCK || id == EFFECT_SETTLE) {
      noiseTimer = SHORT_EFFECT_FRAMES;
      write(NR41, NOISE_LENGTH);
      write(NR42, id == EFFECT_LOCK ? EFFECT_ENVELOPE : SETTLE_ENVELOPE);
      write(NR43, id == EFFECT_LOCK ? LOCK_POLYNOMIAL : SETTLE_POLYNOMIAL);
      write(NR44, EFFECT_TRIGGER);
    } else {
      effectTimer = (byte) (id == EFFECT_CLEAR || id == EFFECT_TETRIS ? CLEAR_EFFECT_FRAMES : SHORT_EFFECT_FRAMES);
      final int frequency = effectFrequency(id);
      write(NR10, 0);
      write(NR11, EFFECT_DUTY);
      write(NR12, EFFECT_ENVELOPE);
      write(NR13, frequency & BYTE_MASK);
      write(NR14, (frequency >>> 8) | EFFECT_TRIGGER);
    }
  }

  @NoInline
  public void tick() {
    tick(false);
  }

  @NoInline
  public void tick(final boolean busy) {
    events = 0;
    vibratoPhase = (byte) ((vibratoPhase + 1) & VIBRATO_PHASE_MASK);
    if (effectTimer > 0) {
      effectTimer--;
    }
    if (noiseTimer > 0) {
      noiseTimer--;
    }
    if (song == 0) {
      return;
    }
    step(one, 0);
    step(two, 1);
    step(three, 2);
    step(four, 3);
    if (song != 0) {
      prepareNext(busy);
    }
  }
}

package probe;
import dev.java2gb.api.*;

import static probe.TetrisLayout.*;
import static probe.TetrisProtocol.*;
import static probe.TetrisHardware.SERIAL_EXTERNAL_CLOCK;
import static probe.TetrisHardware.SERIAL_INTERNAL_CLOCK;
import static probe.TetrisHardware.SERIAL_MANUAL;
import static probe.TetrisHardware.SERIAL_MENU;
import static probe.TetrisHardware.SERIAL_GAME;

public final class TetrisMultiplayer {
  @RomData private static final byte[] PAUSE = RomText.ascii("PAUSE");
  @RomData private static final byte[] WIN = RomText.ascii("YOU WIN");
  @RomData private static final byte[] LOSE = RomText.ascii("YOU LOSE");
  @RomData private static final byte[] DRAW = RomText.ascii("DRAW");
  @RomData private static final byte[] START = RomText.ascii("PUSH START");
  public final byte[] pieces = new byte[PIECE_SEQUENCE_LENGTH];
  public final byte[] garbage = new byte[GARBAGE_CELLS];
  public byte stage = TITLE, height, peerHeight, music, wins, theirWins, outcome;
  public byte gauge = -1, peerStack, pendingAttack, incomingAttack;
  private byte tx, savedTx, tempPreview, nextPreview;
  private boolean starting, armed, draw, endStreaming, resultSent;
  public boolean master, connected;
  public short delay;
  public int sentBytes, receivedBytes, rounds;

  @NoInline
  public void title() {
    TetrisHardware.serialOpen();
    TetrisHardware.serialReload(0, SERIAL_MANUAL);
    TetrisHardware.serialArm(GUEST_HELLO, SERIAL_EXTERNAL_CLOCK);
    stage = TITLE;
    connected = false;
    armed = true;
  }

  @NoInline
  public boolean titleTick() {
    if (!armed) {
      title();
    }
    final int event = TetrisHardware.serialRead();
    if (event >= 0) {
      final int value = receivedByte(event);
      if (value == HOST_HELLO || (stage == HANDSHAKE && value == GUEST_HELLO)) {
        master = value == GUEST_HELLO;
        connected = true;
        wins = 0;
        theirWins = 0;
        height = 0;
        peerHeight = 0;
        rounds = 0;
        musicScreen();
        return true;
      }
      stage = TITLE;
      TetrisHardware.serialArm(GUEST_HELLO, SERIAL_EXTERNAL_CLOCK);
    }
    return false;
  }

  @NoInline
  public void lobby() {
    stage = HANDSHAKE;
    TetrisHardware.serialReload(0, SERIAL_MANUAL);
    TetrisHardware.serialArm(HOST_HELLO, SERIAL_INTERNAL_CLOCK);
    sentBytes++;
  }

  private void output(final int value) {
    tx = (byte) value;
    if (master) {
      TetrisHardware.serialSchedule(value);
    } else {
      TetrisHardware.serialReload(value, (stage == PLAY || (stage == RESULT_BARRIER && !endStreaming)) ? SERIAL_GAME : SERIAL_MENU);
    }
  }

  private boolean matchComplete() {
    return wins == MATCH_COMPLETE || theirWins == MATCH_COMPLETE;
  }

  private static int displayWins(final int value) {
    return value == MATCH_COMPLETE ? MATCH_WINS : value;
  }

  @NoInline
  private void musicScreen() {
    stage = MUSIC;
    starting = false;
    TetrisGame.scene = TetrisGame.SCENE_LINK;
    TetrisGame.paused = false;
    if (!master) {
      TetrisHardware.serialReload(MUSIC_REPLY, SERIAL_MENU);
      TetrisHardware.serialArm(MUSIC_REPLY, SERIAL_EXTERNAL_CLOCK);
    }
    TetrisGame.video.screen(CONFIG_SCREEN);
    musicCursor();
    TetrisGame.video.show();
    TetrisGame.sound.play(TetrisSound.selectionSong(music));
    if (master) {
      output(MUSIC_FIRST + music);
    }
  }

  private void musicCursor() {
    TetrisMenus.musicLabel(0, music, master);
  }

  @Mutable
  @NoInline
  private void generatePieces() {
    for (int index = -RANDOMIZER_WARMUP; index < PIECE_SEQUENCE_LENGTH; index++) {
      int candidate = 0;
      for (int attempt = 0; attempt < TetrisRules.RANDOM_ATTEMPTS; attempt++) {
        candidate = TetrisRules.pieceFromRandom(TetrisHardware.divider());
        if (attempt == TetrisRules.RANDOM_ATTEMPTS - 1
            || TetrisRules.candidateAllowed(candidate, tempPreview & TetrisRules.PIECE_TYPE_MASK, nextPreview)) {
          break;
        }
      }
      tempPreview = nextPreview;
      nextPreview = (byte) candidate;
      if (index >= 0) {
        pieces[index] = tempPreview;
      }
    }
  }

  @Mutable
  @NoInline
  private void generateGarbage() {
    for (int row = 0; row < GARBAGE_TEMPLATE_ROWS; row++) {
      boolean hole = false;
      for (int col = 0; col < FIELD_COLUMNS; col++) {
        int value = (TetrisHardware.divider() & 1) == 0 ? EMPTY_TILE : FIRST_BLOCK_TILE | (TetrisHardware.divider() & BLOCK_VARIANT_MASK);
        if (value == EMPTY_TILE) {
          hole = true;
        }
        if (col == FIELD_COLUMNS - 1 && !hole) {
          value = EMPTY_TILE;
        }
        garbage[row * FIELD_COLUMNS + col] = (byte) value;
      }
    }
  }

  @Mutable
  @NoInline
  private void heights() {
    stage = HEIGHTS;
    if (matchComplete()) {
      wins = 0;
      theirWins = 0;
    }
    starting = false;
    outcome = 0;
    draw = false;
    pendingAttack = 0;
    incomingAttack = 0;
    TetrisGame.paused = false;
    if (master) {
      generatePieces();
    } else {
      TetrisHardware.serialReload(height, SERIAL_MENU);
    }
    TetrisGame.video.screen(LINK_HEIGHT_SCREEN);
    cursors();
    TetrisGame.video.show();
    if (master && (tx & BYTE_MASK) != REMATCH_ACK) {
      output(height);
    }
  }

  @NoInline
  private void cursors() {
    final int mario = master ? height : peerHeight, luigi = master ? peerHeight : height;
    TetrisMenus.gridDigit(0, mario, HEIGHT_GRID_COLUMNS, HEIGHT_DIGIT_X, MARIO_FACE_Y, master);
    TetrisMenus.gridDigit(1, luigi, HEIGHT_GRID_COLUMNS, HEIGHT_DIGIT_X, LUIGI_FACE_Y, !master);
    TetrisGame.video.face(MENU_FACE_SLOT, MENU_FACE_X, MARIO_FACE_Y, MARIO_FACE_TILE);
    TetrisGame.video.face(MENU_FACE_SLOT + FACE_SPRITES, MENU_FACE_X, LUIGI_FACE_Y, LUIGI_FACE_TILE);
  }

  private int exchange(final int value) {
    TetrisHardware.serialArm(value, master ? SERIAL_INTERNAL_CLOCK : SERIAL_EXTERNAL_CLOCK);
    sentBytes++;
    int event;
    do {
      event = TetrisHardware.serialRead();
    } while (event < 0);
    receivedBytes++;
    return receivedByte(event);
  }

  private void rendezvous(final int send, final int expected) {
    int value;
    do {
      TetrisHardware.serialDelay();
      if (master) {
        TetrisHardware.serialDelay();
      }
      value = exchange(send);
    } while (value != expected);
  }

  private void drainReplies() {
    while (TetrisHardware.serialRead() >= 0) {
    }
  }

  private void transfer(@Mutable final byte[] payload, final int length) {
    for (int index = 0; index < length; index++) {
      if (master) {
        TetrisHardware.serialDelay();
        exchange(payload[index] & BYTE_MASK);
      } else {
        payload[index] = (byte) exchange(0);
      }
    }
  }

  @Mutable
  @NoInline
  private void prepare() {
    TetrisHardware.serialReload(0, SERIAL_MANUAL);
    TetrisHardware.serialVideo(0);
    drainReplies();
    if (master) {
      generateGarbage();
    }
    TetrisGame.video.screen(LINK_GAME_SCREEN);
    drainReplies();
    rendezvous(master ? HOST_HELLO : GUEST_HELLO, master ? GUEST_HELLO : HOST_HELLO);
    transfer(garbage, GARBAGE_CELLS);
    rendezvous(master ? HOST_HELLO : GUEST_HELLO, master ? GUEST_HELLO : HOST_HELLO);
    transfer(pieces, PIECE_SEQUENCE_LENGTH);
    rendezvous(master ? SETUP_DONE : SETUP_REPLY, master ? SETUP_REPLY : SETUP_DONE);
    final TetrisRules g = TetrisGame.game;
    g.sequence = pieces;
    g.loadMatch(height, garbage);
    pendingAttack = 0;
    incomingAttack = 0;
    gauge = -1;
    TetrisGame.video.number(HEIGHT_X, HEIGHT_Y, 1, height);
    face();
    for (int row = 0; row < FIELD_ROWS; row++) {
      TetrisGame.video.row(g, row, false);
    }
    TetrisGame.video.game(g, TetrisGame.preview);
    TetrisGame.video.show();
    TetrisHardware.serialVideo(1);
    stage = READY;
    if (!master) {
      TetrisHardware.serialReload(READY_REPLY, SERIAL_MENU);
      TetrisHardware.serialArm(SETUP_DONE, SERIAL_EXTERNAL_CLOCK);
    } else {
      output(BARRIER_POLL);
    }
    rounds++;
  }

  @NoInline
  private void face() {
    TetrisGame.video.face(GAME_FACE_SLOT, GAME_FACE_X, GAME_FACE_Y,
        master ? MARIO_FACE_TILE : LUIGI_FACE_TILE);
  }

  private boolean barrier(final int value, final int expected, final int accepted) {
    if (master) {
      if (value == expected) {
        output(accepted);
        return true;
      }
      output(BARRIER_POLL);
    } else {
      if (value == accepted) {
        return true;
      }
      output(expected);
    }
    return false;
  }

  private void beginBattle() {
    stage = PLAY;
    if (!master) {
      TetrisHardware.serialReload(0, SERIAL_GAME);
    }
    TetrisGame.lastClock = (char) TetrisHardware.clock();
    TetrisGame.sound.play(TetrisSound.selectionSong(music));
  }

  private void pause(final boolean value) {
    TetrisGame.paused = value;
    if (value) {
      TetrisGame.video.text(PAUSE_TEXT_X, PAUSE_TEXT_Y, PAUSE);
    } else {
      TetrisHardware.backgroundRun(PAUSE_TEXT_X, PAUSE_TEXT_Y, PAUSE.length, HUD_FILL_TILE);
    }
    TetrisHardware.audio(TetrisHardware.NR51, value ? TetrisHardware.AUDIO_ROUTE_NONE : TetrisHardware.AUDIO_ROUTE_ALL);
  }

  private void finish(final int value, final boolean local) {
    outcome = (byte) value;
    stage = RESULT_BARRIER;
    delay = (short) (!local ? 0 : value == LOSS_OUTCOME ? LOSS_DELAY : WIN_DELAY);
    endStreaming = false;
    if (local) {
      output(terminalCommand(value));
    }
    TetrisGame.paused = false;
    TetrisHardware.audio(TetrisHardware.NR51, TetrisHardware.AUDIO_ROUTE_ALL);
  }

  @NoInline
  private void result() {
    stage = RESULT;
    if (!draw) {
      if (outcome == WIN_OUTCOME) {
        wins++;
      } else {
        theirWins++;
      }
    }
    if (wins == MATCH_WINS) {
      wins = MATCH_COMPLETE;
    }
    if (theirWins == MATCH_WINS) {
      theirWins = MATCH_COMPLETE;
    }
    TetrisGame.video.screen(RESULT_SCREEN);
    TetrisGame.video.text(RESULT_TEXT_X, RESULT_TEXT_Y, draw ? DRAW : outcome == WIN_OUTCOME ? WIN : LOSE);
    TetrisGame.video.text(RESULT_TEXT_X, RESULT_PROMPT_Y, START);
    TetrisGame.video.number(RESULT_OUR_WINS_X, RESULT_WINS_Y, 1, displayWins(wins));
    TetrisGame.video.number(RESULT_THEIR_WINS_X, RESULT_WINS_Y, 1, displayWins(theirWins));
    TetrisGame.video.show();
    TetrisGame.sound.play(outcome == WIN_OUTCOME ? TetrisSound.SONG_CLEAR : TetrisSound.SONG_OVER);
    delay = 0;
    resultSent = false;
  }

  private void updateGauge() {
    if (peerStack == gauge) {
      return;
    }
    for (int i = 0; i < FIELD_ROWS; i++) {
      TetrisHardware.sprite(GAUGE_SLOT + i, GAUGE_X,
          i < peerStack ? (FIELD_ROWS - 1 - i) * TILE_PIXELS : HIDDEN_SPRITE_Y,
          GAUGE_TILE, SPRITE_BEHIND_BACKGROUND);
    }
    gauge = peerStack;
  }

  @NoInline
  private void battle(final int buttons, final int pressed, final boolean present, final int received) {
    final TetrisRules g = TetrisGame.game;
    if (master && (pressed & GameBoy.START) != 0) {
      if (TetrisGame.paused) {
        pause(false);
        output(savedTx & BYTE_MASK);
        awaitSent(savedTx & BYTE_MASK);
        output(g.stackHeight());
        return;
      }
      savedTx = tx;
      pause(true);
    }
    if (TetrisGame.paused) {
      if (received >= 0) {
        if (master) {
          output(PAUSED);
        } else if (received != PAUSED) {
          pause(false);
          return;
        } else {
          output(0);
        }
      }
      if (TetrisGame.paused) {
        return;
      }
    }
    if ((pressed & GameBoy.SELECT) != 0) {
      TetrisGame.preview = !TetrisGame.preview;
    }
    final int before = g.phase;
    TetrisGame.advance(buttons, present);
    if (before == TetrisRules.LOCK && g.phase == TetrisRules.CLEAR) {
      pendingAttack = (byte) TetrisRules.attack(g.cleared);
    }
    if (g.phase == TetrisRules.OVER || g.phase == TetrisRules.WON) {
      finish(g.phase == TetrisRules.OVER ? LOSS_OUTCOME : WIN_OUTCOME, true);
      return;
    }
    if (received < 0) {
      return;
    }
    if (received == PAUSED && !master) {
      pause(true);
      output(0);
      return;
    }
    if (received == LOST || received == WON) {
      finish(received == LOST ? WIN_OUTCOME : LOSS_OUTCOME, false);
      return;
    }
    if (received <= FIELD_ROWS) {
      peerStack = (byte) received;
    } else if ((received & ATTACK_FLAG) != 0 && (received & ATTACK_MASK) < ATTACK_LIMIT) {
      incomingAttack = (byte) (received & ATTACK_MASK);
    }
    if (g.garbagePending == 0 && g.garbageCursor < 0 && incomingAttack != 0) {
      g.garbagePending = incomingAttack;
      incomingAttack = 0;
    }
    updateGauge();
    output(pendingAttack != 0 ? ATTACK_FLAG | pendingAttack : g.stackHeight());
    pendingAttack = 0;
  }

  @NoInline
  private boolean musicTick(final int pressed, final int event, final int value) {
    final int old = music;
    if (master) {
      if ((pressed & GameBoy.RIGHT) != 0 && (music & 1) == 0) {
        music++;
      } else if ((pressed & GameBoy.LEFT) != 0 && (music & 1) != 0) {
        music--;
      } else if ((pressed & GameBoy.DOWN) != 0 && music < 2) {
        music += 2;
      } else if ((pressed & GameBoy.UP) != 0 && music >= 2) {
        music -= 2;
      }
      if ((pressed & (GameBoy.START | GameBoy.A)) != 0) {
        starting = true;
        output(SELECT_HEIGHTS);
      } else if (value >= 0) {
        if (starting && transmittedByte(event) == SELECT_HEIGHTS) {
          heights();
          return true;
        }
        output(starting ? SELECT_HEIGHTS : MUSIC_FIRST + music);
      }
    } else if (value == SELECT_HEIGHTS) {
      heights();
      return true;
    } else if (value >= MUSIC_FIRST && value <= MUSIC_LAST) {
      music = (byte) (value - MUSIC_FIRST);
      output(MUSIC_REPLY);
    }
    if (music != old) {
      TetrisGame.sound.play(TetrisSound.selectionSong(music));
    }
    if (master || music != old) {
      musicCursor();
    }
    return false;
  }

  @NoInline
  private boolean heightTick(final int pressed, final int event, final int value) {
    if ((pressed & GameBoy.RIGHT) != 0 && height < MAX_HEIGHT) {
      height++;
    } else if ((pressed & GameBoy.LEFT) != 0 && height > 0) {
      height--;
    } else if ((pressed & GameBoy.DOWN) != 0 && height < HEIGHT_GRID_COLUMNS) {
      height += HEIGHT_GRID_COLUMNS;
    } else if ((pressed & GameBoy.UP) != 0 && height >= HEIGHT_GRID_COLUMNS) {
      height -= HEIGHT_GRID_COLUMNS;
    }
    if (value >= 0 && value <= MAX_HEIGHT) {
      peerHeight = (byte) value;
    }
    if (master && (pressed & GameBoy.START) != 0) {
      starting = true;
      output(START_ROUND);
    } else if (value >= 0) {
      if ((master && starting && transmittedByte(event) == START_ROUND) || (!master && value == START_ROUND)) {
        prepare();
        return true;
      }
      output(starting && master ? START_ROUND : height);
    }
    cursors();
    return false;
  }

  @NoInline
  private boolean resultBarrierTick(final int value) {
    if (value == terminalCommand(outcome)) {
      draw = true;
    }
    if (delay > 0) {
      delay--;
    } else {
      if (!endStreaming) {
        endStreaming = true;
        if (!master) {
          TetrisHardware.serialReload(RESULT_REPLY, SERIAL_MENU);
        } else {
          output(BARRIER_POLL);
        }
      }
      if (value >= 0 && barrier(value, RESULT_REPLY, RESULT_ACK)) {
        stage = RESULT_WAIT;
        delay = RESULT_DELAY;
        return true;
      }
    }
    return false;
  }

  @NoInline
  private boolean resultWaitTick() {
    if (--delay == 0) {
      result();
      return true;
    }
    return false;
  }

  @NoInline
  private void resultTick(final int pressed, final int value) {
    if (delay < RESULT_TIMER_LIMIT) {
      delay++;
    }
    if (master && !resultSent && (matchComplete()
        ? delay >= MATCH_ANIMATION_DELAY : (pressed & GameBoy.START) != 0)) {
      resultSent = true;
      output(START_ROUND);
    } else if (value >= 0 && ((master && resultSent) || (!master && value == START_ROUND))) {
      stage = REMATCH_BARRIER;
      if (master) {
        output(BARRIER_POLL);
      } else {
        output(REMATCH_REPLY);
      }
    }
  }

  @NoInline
  private void rematchTick(final int value) {
    if (barrier(value, REMATCH_REPLY, REMATCH_ACK)) {
      if (matchComplete()) {
        heights();
      } else {
        if (master) {
          generatePieces();
          awaitSent(REMATCH_ACK);
        }
        prepare();
      }
    }
  }

  private int readEvent() {
    int event = TetrisHardware.serialRead();
    if (stage == MUSIC || stage == HEIGHTS) {
      final int command = stage == MUSIC ? SELECT_HEIGHTS : START_ROUND;
      while (event >= 0 && (master ? !(starting && transmittedByte(event) == command)
          : receivedByte(event) != command)) {
        final int next = TetrisHardware.serialRead();
        if (next < 0) {
          break;
        }
        event = next;
        receivedBytes++;
      }
    }
    if (event >= 0) {
      receivedBytes++;
    }
    return event;
  }

  private void awaitSent(final int command) {
    int event;
    do {
      event = TetrisHardware.serialRead();
    } while (event < 0 || transmittedByte(event) != command);
  }

  @NoInline
  public void tick(final int buttons, final int pressed, final boolean present) {
    final int event = readEvent(), value = receivedByte(event);
    if (stage == MUSIC) {
      if (musicTick(pressed, event, value)) {
        return;
      }
    } else if (stage == HEIGHTS) {
      if (heightTick(pressed, event, value)) {
        return;
      }
    } else if (stage == READY && value >= 0) {
      if (barrier(value, READY_REPLY, READY_ACK)) {
        beginBattle();
      }
    } else if (stage == PLAY) {
      battle(buttons, pressed, present, value);
      return;
    } else if (stage == RESULT_BARRIER) {
      if (resultBarrierTick(value)) {
        return;
      }
    } else if (stage == RESULT_WAIT) {
      if (resultWaitTick()) {
        return;
      }
    } else if (stage == RESULT) {
      resultTick(pressed, value);
    } else if (stage == REMATCH_BARRIER && value >= 0) {
      rematchTick(value);
    }
    if (!TetrisGame.paused) {
      TetrisGame.sound.tick();
    }
  }
}

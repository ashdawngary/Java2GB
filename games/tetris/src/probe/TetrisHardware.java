package probe;

public final class TetrisHardware {
  private TetrisHardware() {}

  public static final int PACKED_TILE_BYTES = 4;
  public static final int LCD_OFF = 0, LCD_ON = 1, CLOCK_MASK = 0xffff;
  public static final int SERIAL_EXTERNAL_CLOCK = 0x80, SERIAL_INTERNAL_CLOCK = 0x81;
  public static final int SERIAL_MANUAL = GameBoySerial.MANUAL, SERIAL_REARM = GameBoySerial.REARM;
  public static final int SERIAL_CLEAR_REPLY = GameBoySerial.CLEAR_REPLY, SERIAL_DELAY_REARM = GameBoySerial.DELAY_REARM;
  public static final int SERIAL_MENU = SERIAL_REARM | SERIAL_DELAY_REARM;
  public static final int SERIAL_GAME = SERIAL_REARM | SERIAL_CLEAR_REPLY;
  public static final int NR10 = 0x10, NR11 = 0x11, NR12 = 0x12, NR13 = 0x13, NR14 = 0x14;
  public static final int NR21 = 0x16, NR22 = 0x17, NR23 = 0x18, NR24 = 0x19;
  public static final int NR30 = 0x1a, NR31 = 0x1b, NR32 = 0x1c, NR33 = 0x1d, NR34 = 0x1e;
  public static final int NR41 = 0x20, NR42 = 0x21, NR43 = 0x22, NR44 = 0x23;
  public static final int NR50 = 0x24, NR51 = 0x25, NR52 = 0x26;
  public static final int WAVE_RAM = 0x30, WAVE_BYTES = 16;
  public static final int AUDIO_ENABLED = 0x80, AUDIO_ROUTE_ALL = 0xff;
  public static final int AUDIO_ROUTE_NONE = 0, AUDIO_MAX_VOLUME = 0x77;
  public static final int NOTE_TRIGGER = 0x80, EFFECT_TRIGGER = 0xc0;


  private static final int BGP = 0xff47, OBP0 = 0xff48, OBP1 = 0xff49, LCDC = 0xff40;
  private static final int DIV = 0xff04, IO_BASE = 0xff00, BACKGROUND = 0x9800, MAP_WIDTH = 32;
  private static final int DMG_MAIN_PALETTE = 0xe4, DMG_ALTERNATE_PALETTE = 0xc4, GAME_LCD_MODE = 0xd3;
  private static final int SPRITE_X_OFFSET = 8, SPRITE_Y_OFFSET = 16;

  public static void tiles(final int set) { TetrisAssets.tiles(set); }

  public static void lcd(final int enabled) {
    if (enabled == LCD_OFF) { GameBoyHardware.lcdOff(); return; }
    GameBoyHardware.write(BGP,DMG_MAIN_PALETTE);
    GameBoyHardware.write(OBP0,DMG_MAIN_PALETTE);
    GameBoyHardware.write(OBP1,DMG_ALTERNATE_PALETTE);
    GameBoyHardware.write(LCDC,GAME_LCD_MODE);
  }

  public static void sprite(final int slot, final int x, final int y, final int tile, final int flags) {
    if (slot < 0 || slot >= TetrisLayout.SPRITE_COUNT) { return; }
    final int packed = ((y + SPRITE_Y_OFFSET) & TetrisLayout.BYTE_MASK)
        | (((x + SPRITE_X_OFFSET) & TetrisLayout.BYTE_MASK) << 8)
        | ((tile & TetrisLayout.BYTE_MASK) << 16) | ((flags & TetrisLayout.BYTE_MASK) << 24);
    GameBoyHardware.oamPacked(slot,packed);
  }

  private static boolean backgroundSpan(final int x, final int y, final int length) {
    return x >= 0 && x < TetrisLayout.SCREEN_COLUMNS && y >= 0 && y < MAP_WIDTH
        && length >= 0 && length <= TetrisLayout.SCREEN_COLUMNS - x;
  }

  public static void backgroundRun(final int x, final int y, final int length, final int tile) {
    if (!backgroundSpan(x,y,length) || tile < 0 || tile > TetrisLayout.BYTE_MASK) {
      GameBoyHardware.trap(2); return;
    }
    if (length != 0) { GameBoyHardware.vramFill(BACKGROUND + y * MAP_WIDTH + x,tile,length); }
  }

  public static void backgroundPacked(final int x, final int y, final int length, final int tiles) {
    if (!backgroundSpan(x,y,length) || length > PACKED_TILE_BYTES) {
      GameBoyHardware.trap(2); return;
    }
    if (length != 0) { GameBoyHardware.vramSpan(BACKGROUND + y * MAP_WIDTH + x,tiles,length); }
  }

  public static void audio(final int register, final int value) {
    if (register >= NR10 && register <= WAVE_RAM + WAVE_BYTES - 1) {
      GameBoyHardware.write(IO_BASE + register,value);
    }
  }

  public static int audioData(final int address) { return TetrisAssets.audioData(address); }
  public static int audioWord(final int address) { return TetrisAssets.audioWord(address); }
  public static int clock() { return GameBoyHardware.clock(); }
  public static int divider() { return GameBoyHardware.read(DIV); }
  public static int entropy() { return ((clock() << 8) | divider()) & CLOCK_MASK; }
  public static void serialOpen() { GameBoySerial.open(); }
  public static void serialClose() { GameBoySerial.close(); }
  public static void serialArm(final int data, final int control) { GameBoySerial.arm(data,control); }
  public static void serialReload(final int data, final int flags) { GameBoySerial.reload(data,flags); }
  public static void serialSchedule(final int data) { GameBoySerial.schedule(data); }
  public static void serialVideo(final int enabled) { GameBoyHardware.vblank(enabled); }
  public static void serialDelay() { GameBoySerial.delay250(); }
  public static int serialRead() { return GameBoySerial.read(); }
}

package probe;

public final class PokemonHardware {
  private PokemonHardware() {}
  private static final int RAM_ENABLE = 0x0000, RAM_BANK = 0x4000, RAM_BASE = 0xa000, RAM_BYTES = 8192;
  private static final int SCY = 0xff42, SCX = 0xff43, WY = 0xff4a, WX = 0xff4b;
  private static final int NR50 = 0xff24, NR51 = 0xff25, NR52 = 0xff26;
  private static final int WINDOW_MAP = 0x9c00, MAP_WIDTH = 32, RUMBLE_FLAG = 8;
  private static byte ramControl;

  public static void init() {
    GameBoyHardware.lcdOff();
    if (GameBoyHardware.color()) {
      GameBoyHardware.fast();
    }
    GameBoyHardware.write(NR52, 0x80);
    GameBoyHardware.write(NR50, 0x77);
    GameBoyHardware.write(NR51, 0xff);
    ramControl = 0;
    GameBoyHardware.write(RAM_BANK, 0);
    GameBoyHardware.raster(255, 0, 0);
  }

  public static int color() { return GameBoyHardware.color() ? 1 : 0; }
  public static int clock() { return GameBoyHardware.clock(); }

  public static void oam(final int slot, final int x, final int y, final int tile, final int flags) {
    oamPacked(slot, (y & 255) | ((x & 255) << 8) | ((tile & 255) << 16) | (flags << 24));
  }

  public static void oamPacked(final int slot, final int packed) {
    GameBoyHardware.oamPacked(slot, packed);
  }

  public static void scroll(final int x, final int y) {
    GameBoyHardware.write(SCY, y);
    GameBoyHardware.write(SCX, x);
  }

  public static void window(final int x, final int y) {
    GameBoyHardware.write(WY, y);
    GameBoyHardware.write(WX, x);
  }

  public static void windowPacked(final int x, final int y, final int tiles) {
    if (x < 0 || x > MAP_WIDTH - 4 || y < 0 || y >= MAP_WIDTH) {
      GameBoyHardware.trap(2);
      return;
    }
    GameBoyHardware.vramPacked(WINDOW_MAP + y * MAP_WIDTH + x, tiles);
  }

  public static void raster(final int line, final int before, final int after) {
    GameBoyHardware.raster(line, before, after);
  }

  public static void audio(final int register, final int value) {
    if (register >= 0x10 && register <= 0x3f) {
      GameBoyHardware.write(0xff00 + register, value);
    }
  }

  public static void rumble(final boolean active) {
    final byte control = (byte) (active ? RUMBLE_FLAG : 0);
    if (ramControl != control) {
      ramControl = control;
      GameBoyHardware.write(RAM_BANK, ramControl);
    }
  }

  public static int saveRead(final int offset) {
    if (offset < 0 || offset >= RAM_BYTES) {
      GameBoyHardware.trap(2);
      return 0;
    }
    GameBoyHardware.write(RAM_ENABLE, 0x0a);
    GameBoyHardware.write(RAM_BANK, ramControl);
    final int value = GameBoyHardware.read(RAM_BASE + offset);
    GameBoyHardware.write(RAM_ENABLE, 0);
    return value;
  }

  public static void saveWrite(final int offset, final int value) {
    if (offset < 0 || offset >= RAM_BYTES) {
      GameBoyHardware.trap(2);
      return;
    }
    GameBoyHardware.write(RAM_ENABLE, 0x0a);
    GameBoyHardware.write(RAM_BANK, ramControl);
    GameBoyHardware.write(RAM_BASE + offset, value);
    GameBoyHardware.write(RAM_ENABLE, 0);
  }
}

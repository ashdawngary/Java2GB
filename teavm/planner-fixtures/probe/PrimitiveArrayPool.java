package probe;

public final class PrimitiveArrayPool {
  @PoolCapacity(2)
  static final class Cell {
    final boolean[] flags;
    final byte[] bytes;
    final short[] shorts;
    final char[] chars;
    final int[] ints;

    Cell(boolean[] flags, byte[] bytes, short[] shorts, char[] chars, int[] ints) {
      this.flags = flags;
      this.bytes = bytes;
      this.shorts = shorts;
      this.chars = chars;
      this.ints = ints;
    }
  }

  static Cell kept, latest;
  static boolean[] flags;
  static byte[] bytes;
  static short[] shorts;
  static char[] chars;
  static int[] ints;

  @NoInline
  static int step(int value) {
    latest = new Cell(flags, bytes, shorts, chars, ints);
    return latest.bytes[2]
        + latest.shorts[1]
        + kept.chars[3]
        + kept.ints[0]
        + (latest.flags[4] ? value : -value);
  }

  @Mutable
  public static int compute() {
    flags = new boolean[5];
    bytes = new byte[5];
    shorts = new short[5];
    chars = new char[5];
    ints = new int[5];
    flags[4] = true;
    bytes[2] = -128;
    shorts[1] = -32768;
    chars[3] = 65535;
    ints[0] = 123456789;
    kept = new Cell(flags, bytes, shorts, chars, ints);
    int result = 0;
    for (int i = 0; i < 1000; i++) {
      result = result * 31 + step(i);
    }
    return result + kept.bytes[2] + latest.chars[3];
  }

  public static void main(String[] args) {
    GameBoy.report(compute());
  }
}

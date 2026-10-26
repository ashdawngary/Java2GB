package probe;

public final class PrimitiveArrays {
  static final class Fields {
    boolean flag;
    byte small;
    short signed;
    char unsigned;
    int wide;
  }

  static boolean[] flags;
  static byte[] bytes;
  static short[] shorts;
  static char[] chars;
  static int[] ints;
  static boolean[] emptyFlags;
  static byte[] emptyBytes;
  static short[] emptyShorts;
  static char[] emptyChars;
  static int[] emptyInts;
  static Fields fields;
  static int checks, failures, hash;

  static void check(boolean value) {
    checks++;
    if (!value) {
      failures++;
    }
    hash = hash * 31 + (value ? checks : -checks);
  }

  @Mutable
  @NoInline
  static void exercise(int value) {
    int b = value % bytes.length;
    int s = value % shorts.length;
    int c = value % chars.length;
    int i = value % ints.length;
    int f = value % flags.length;
    int input = value * 257 - 65537;
    bytes[b] = (byte) input;
    shorts[s] = (short) input;
    chars[c] = (char) input;
    ints[i] = input;
    flags[f] = (value & 1) != 0;
    fields.flag = flags[f];
    fields.small = bytes[b];
    fields.signed = shorts[s];
    fields.unsigned = chars[c];
    fields.wide = ints[i];
    check(bytes[b] == (input << 24 >> 24));
    check((bytes[b] & 255) == (input & 255));
    check(shorts[s] == (input << 16 >> 16));
    check(chars[c] == (input & 65535));
    check(flags[f] == ((value & 1) != 0));
    check(fields.flag == flags[f] && fields.small == bytes[b]);
    check(fields.signed == shorts[s] && fields.unsigned == chars[c]);
    check(fields.wide == input);
    bytes[b] = 127;
    shorts[s] = 32767;
    chars[c] = 65535;
    check(bytes[b] + 1 == 128);
    check(shorts[s] + 1 == 32768);
    check(chars[c] + 1 == 65536);
    bytes[b]++;
    shorts[s]++;
    chars[c]++;
    check(bytes[b] == -128 && shorts[s] == -32768 && chars[c] == 0);
    hash = hash * 31 + bytes[b] + shorts[s] + chars[c] + ints[i];
  }

  @Mutable
  @NoInline
  static int reuse(int value) {
    byte[] b = new byte[5];
    check(b[0] == 0 && b[4] == 0);
    b[4] = (byte) value;
    int result = b[4];
    short[] s = new short[5];
    check(s[0] == 0 && s[4] == 0);
    s[4] = (short) (value * 257);
    result += s[4];
    char[] c = new char[5];
    check(c[0] == 0 && c[4] == 0);
    c[4] = (char) (value - 257);
    result += c[4];
    boolean[] f = new boolean[5];
    check(!f[0] && !f[4]);
    f[4] = (value & 1) == 0;
    result += f[4] ? 17 : 3;
    int[] i = new int[5];
    check(i[0] == 0 && i[4] == 0);
    i[4] = value * 65537;
    return result + i[4];
  }

  public static int compute() {
    checks = 0;
    failures = 0;
    hash = 17;
    flags = new boolean[17];
    bytes = new byte[19];
    shorts = new short[11];
    chars = new char[13];
    ints = new int[7];
    emptyFlags = new boolean[0];
    emptyBytes = new byte[0];
    emptyShorts = new short[0];
    emptyChars = new char[0];
    emptyInts = new int[0];
    fields = new Fields();
    check(emptyFlags.length == 0 && emptyBytes.length == 0 && emptyShorts.length == 0);
    check(emptyChars.length == 0 && emptyInts.length == 0);
    check(!fields.flag && fields.small == 0 && fields.signed == 0);
    check(fields.unsigned == 0 && fields.wide == 0);
    for (int i = 0; i < 19; i++) {
      check(bytes[i] == 0);
    }
    for (int i = 0; i < 17; i++) {
      check(!flags[i]);
    }
    for (int i = 0; i < 11; i++) {
      check(shorts[i] == 0);
    }
    for (int i = 0; i < 13; i++) {
      check(chars[i] == 0);
    }
    for (int i = 0; i < 7; i++) {
      check(ints[i] == 0);
    }
    for (int value = 0; value < 513; value++) {
      exercise(value);
    }
    for (int value = 0; value < 200; value++) {
      hash = hash * 31 + reuse(value);
    }
    return hash;
  }

  public static void main(String[] args) {
    GameBoy.report(compute());
  }
}

package probe;

public final class RomArrayProbe {
    @RomData(bank = 0) static final boolean[] FLAGS = {false, true, true, false};
    @RomData(bank = 0) static final byte[] BYTES = {0, 1, 127, -128, -1};
    @RomData(bank = 2) static final short[] SHORTS = {0, 1, 32767, -32768, -1};
    @RomData(bank = 3) static final char[] CHARS = {0, 1, 32767, 32768, 65535};
    @RomData static final int[] INTS = {0, 1, Integer.MAX_VALUE, Integer.MIN_VALUE, -1};
    @RomData static final byte[] OTHER = {7, -8, 9, -10, 11};
    @RomData static final byte[] EMPTY = {};
    @RomData static final byte[] LARGE = new byte[12000];
    static int assertions, failures;

    static final class Box {
        byte[] data;
        Box(byte[] data) { this.data = data; }
    }
    private static void check(boolean value) {
        assertions++;
        if (!value) { failures++; }
    }
    @NoInline static byte[] returned(boolean other) { return other ? OTHER : BYTES; }
    @NoInline static int throughBox(Box box, int i) { return RomArrayReaders.byteAt(box.data, i); }
    @NoInline static void writeRam(@Mutable byte[] array, int i, byte value) { array[i] = value; }

    public static int run() {
        int hash = 1;
        byte[] ram = new byte[5];
        Box box = new Box(BYTES);
        check(EMPTY.length == 0);
        check(LARGE.length == 12000 && LARGE[0] == 0 && LARGE[11999] == 0);
        check(FLAGS.length == 4 && BYTES.length == 5 && SHORTS.length == 5
                && CHARS.length == 5 && INTS.length == 5);
        for (int round = 0; round < 100; round++) {
            for (int i = 0; i < 5; i++) {
                int b = RomArrayReaders.byteAt(BYTES, i);
                int s = RomArrayReaders.shortAt(SHORTS, i);
                int c = RomArrayReaders.charAt(CHARS, i);
                int n = RomArrayReaders.intAt(INTS, i);
                boolean flag = RomArrayReaders.flagAt(FLAGS, i & 3);
                int expectedByte = i == 0 ? 0 : i == 1 ? 1 : i == 2 ? 127 : i == 3 ? -128 : -1;
                int expectedShort = i == 0 ? 0 : i == 1 ? 1 : i == 2 ? 32767 : i == 3 ? -32768 : -1;
                int expectedChar = i == 0 ? 0 : i == 1 ? 1 : i == 2 ? 32767 : i == 3 ? 32768 : 65535;
                int expectedInt = i == 0 ? 0 : i == 1 ? 1 : i == 2 ? Integer.MAX_VALUE : i == 3 ? Integer.MIN_VALUE : -1;
                check(b == expectedByte);
                check(s == expectedShort);
                check(c == expectedChar);
                check(n == expectedInt);
                check(flag == ((i & 3) == 1 || (i & 3) == 2));
                check(throughBox(box, i) == b);
                writeRam(ram, i, (byte) round);
                check(RomArrayReaders.byteAt(ram, i) == (byte) round);
                byte[] selected = (round & 1) == 0 ? ram : returned((round & 2) != 0);
                int expected = (round & 1) == 0 ? (byte) round : (round & 2) == 0 ? b
                        : i == 0 ? 7 : i == 1 ? -8 : i == 2 ? 9 : i == 3 ? -10 : 11;
                check(RomArrayReaders.byteAt(selected, i) == expected);
                hash = hash * 31 + b + s + c + n + (flag ? 1 : 0) + expected;
            }
        }
        check(BYTES == returned(false) && BYTES != OTHER && BYTES != ram);
        check("ROM".charAt(0) == 'R');
        return failures == 0 ? hash : -failures;
    }
    public static void main(String[] args) { GameBoy.report(run()); }
}

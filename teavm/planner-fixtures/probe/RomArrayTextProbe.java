package probe;

public final class RomArrayTextProbe {
    @RomData(bank = 0) static final byte[] ASCII = RomText.ascii("A\0\u007f\"\\\n");
    @RomData(bank = 2) static final byte[] BANKED = RomText.ascii("PRESS " + "START");
    @RomData(bank = 3) static final char[] UNICODE = RomText.chars("A\u00e9\u03a9\ud83d\ude00\ud800\udfff\0");
    @RomData(bank = 0) static final char[] WIDE_ASCII = RomText.chars("ABC");
    @RomData static final byte[] EMPTY = RomText.ascii("");
    @RomData static final char[] EMPTY_CHARS = RomText.chars("");
    @RomData static final byte[] FIRST = RomText.ascii("SAME");
    @RomData static final byte[] SECOND = RomText.ascii("SAME");
    @RomData static final byte[] BINARY = RomText.bytes("\000\177\200\377");
    static int assertions, failures;

    static void check(boolean value) {
        assertions++;
        if (!value) { failures++; }
    }

    @NoInline static int readByte(byte[] value, int index) { return value[index]; }
    @NoInline static int readChar(char[] value, int index) { return value[index]; }

    public static int run() {
        check(BINARY.length == 4 && readByte(BINARY,0)==0 && readByte(BINARY,1)==127
                && readByte(BINARY,2)==-128 && readByte(BINARY,3)==-1);
        check(ASCII.length == 6 && EMPTY.length == 0 && EMPTY_CHARS.length == 0);
        check(UNICODE.length == 8 && WIDE_ASCII.length == 3);
        check(FIRST != SECOND && FIRST == FIRST);
        check(ASCII[0] == 65 && ASCII[1] == 0 && ASCII[2] == 127
                && ASCII[3] == 34 && ASCII[4] == 92 && ASCII[5] == 10);
        check(UNICODE[1] == 233 && UNICODE[2] == 937 && UNICODE[3] == 0xd83d
                && UNICODE[4] == 0xde00 && UNICODE[5] == 0xd800
                && UNICODE[6] == 0xdfff && UNICODE[7] == 0);
        check("UNICODE \u03a9".charAt(8) == 937);
        int hash = 17;
        for (int round = 0; round < 100; round++) {
            for (int i = 0; i < BANKED.length; i++) {
                check(readByte(BANKED, i) == "PRESS START".charAt(i));
                hash = hash * 31 + readByte(BANKED, i);
            }
            for (int i = 0; i < UNICODE.length; i++) {
                hash = hash * 31 + readChar(UNICODE, i);
            }
            for (int i = 0; i < WIDE_ASCII.length; i++) {
                check(readChar(WIDE_ASCII, i) == 65 + i);
            }
        }
        return failures == 0 ? hash : -failures;
    }

    public static void main(String[] args) { GameBoy.report(run()); }
}

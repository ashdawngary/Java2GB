package probe;
import dev.java2gb.api.*;
public final class StaticStringProbe {
    static final String TITLE = "POCKET PINBALL";
    static final String COMBINED = "POCKET" + " PINBALL";
    static String greeting = "HELLO";
    static String unusedDynamic(int value) { return "UNREACHABLE " + value; }
    static final class Reader {
        @NoInline static boolean matches(String text) { return text == "HELLO"; }
        @NoInline static int sample(String text, int index) { return text.length()*100 + text.charAt(index); }
    }
    public static int compute() {
        int checks=0;
        if (TITLE.length()==14) checks |= 1;
        if (Reader.sample(greeting,1)==569) checks |= 2;
        if ("".isEmpty() && !TITLE.isEmpty()) checks |= 4;
        if ("A\u0000Z".charAt(1)==0) checks |= 8;
        if ("\u03a9\ud83c\udfb1".length()==3 && "\u03a9\ud83c\udfb1".charAt(0)==937
                && "\u03a9\ud83c\udfb1".charAt(1)==55356 && "\u03a9\ud83c\udfb1".charAt(2)==57265) checks |= 16;
        String same = greeting;
        if (same == "HELLO") checks |= 32;
        if (COMBINED == TITLE) checks |= 64;
        if (Reader.matches(greeting)) checks |= 128;
        return checks;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

package probe;
import dev.java2gb.api.*;

public final class RomBankingProbe {
    static final class Alpha {
        static int seed = Gamma.initialValue();
        @NoInline static int call(int a, int b, int c, int d) {
            int saved = a + seed;
            int result = Beta.call(saved, b, c, d);
            return result ^ finish(saved, d);
        }
        @NoInline static int finish(int a, int b) { return a * 37 + b; }
    }
    static final class Beta {
        static int calls;
        @NoInline static int call(int a, int b, int c, int d) {
            calls++;
            int saved = b - calls;
            return Gamma.call(a, saved, c, d) + saved;
        }
    }
    static final class Gamma {
        static int calls;
        @NoInline static int initialValue() { return 12345; }
        @NoInline static int call(int a, int b, int c, int d) {
            calls++;
            return Alpha.finish(a ^ c, b) - d + calls;
        }
    }
    public static int compute() {
        int checksum = 0;
        for (int i = 0; i < 128; i++) {
            checksum = checksum * 31 + Alpha.call(i - 75, i * 1003, -70001, 90007 - i);
        }
        return checksum ^ Beta.calls ^ (Gamma.calls << 8);
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

package probe;
import dev.java2gb.api.*;

public final class ArrayAccessBench {
    private ArrayAccessBench() {}
    static final int COUNT = 256, MASK = 63;
    @RomData(bank=0) static final byte[] FIXED = {
        3,20,37,54,71,88,105,122,-117,-100,-83,-66,-49,-32,-15,2,
        19,36,53,70,87,104,121,-118,-101,-84,-67,-50,-33,-16,1,18,
        35,52,69,86,103,120,-119,-102,-85,-68,-51,-34,-17,0,17,34,
        51,68,85,102,119,-120,-103,-86,-69,-52,-35,-18,-1,16,33,50};
    @RomData(bank=2) static final byte[] BANKED = {
        3,20,37,54,71,88,105,122,-117,-100,-83,-66,-49,-32,-15,2,
        19,36,53,70,87,104,121,-118,-101,-84,-67,-50,-33,-16,1,18,
        35,52,69,86,103,120,-119,-102,-85,-68,-51,-34,-17,0,17,34,
        51,68,85,102,119,-120,-103,-86,-69,-52,-35,-18,-1,16,33,50};
    static final byte[] RAM = new byte[MASK + 1];
    public static final int[] results = new int[8];
    public static int seed;

    @NoInline public static int indexOnly(final int start) {
        int sum = 0;
        for (int i = 0; i < COUNT; i++) { sum += (i + start) & MASK; }
        return sum;
    }
    @NoInline public static int ram(final int start) {
        int sum = 0;
        for (int i = 0; i < COUNT; i++) { sum += RAM[(i + start) & MASK] & 255; }
        return sum;
    }
    @NoInline public static int fixed(final int start) {
        int sum = 0;
        for (int i = 0; i < COUNT; i++) { sum += FIXED[(i + start) & MASK] & 255; }
        return sum;
    }
    @NoInline public static int banked(final int start) {
        int sum = 0;
        for (int i = 0; i < COUNT; i++) { sum += BANKED[(i + start) & MASK] & 255; }
        return sum;
    }
    @NoInline static int byteAt(final byte[] source, final int index) { return source[index] & 255; }
    @NoInline public static int generic(final byte[] source, final int start) {
        int sum = 0;
        for (int i = 0; i < COUNT; i++) { sum += byteAt(source,(i + start) & MASK); }
        return sum;
    }
    @NoInline public static int packed(final byte[] source, final int start) {
        int sum = 0;
        for (int i = 0; i < COUNT; i += 4) { sum += RomMemory.readIntLE(source,(i + start) & (MASK - 3)); }
        return sum;
    }
    @Mutable public static void main(final String[] args) {
        GameBoy.init();
        GameBoyHardware.write(0xffff,0);
        for (int i = 0; i < RAM.length; i++) { RAM[i] = (byte) (i * 17 + 3); }
        seed = GameBoyHardware.read(0xff04);
        results[0] = indexOnly(seed);
        results[1] = ram(seed);
        results[2] = fixed(seed);
        results[3] = banked(seed);
        results[4] = generic(RAM,seed);
        results[5] = generic(BANKED,seed);
        results[6] = packed(RAM,seed);
        results[7] = packed(BANKED,seed);
        GameBoy.report(1);
        while (true) { GameBoy.waitFrame(); }
    }
}

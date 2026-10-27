package probe;
import dev.java2gb.api.*;

public final class RomArrayFault {
    @RomData static final byte[] BYTES = {1, 2};
    @RomData static final short[] SHORTS = {1, 2};
    @RomData static final char[] CHARS = {1, 2};
    @RomData static final int[] INTS = {1, 2};
    @RomData static final boolean[] FLAGS = {false, true};
    @RomData static final byte[] EMPTY = {};
    @RomData(bank = 0) static final char[] FIXED = {32768, 65535};
    public static int kind, scenario, index;
    public static void main(String[] args) {
        while (GameBoy.buttons() == 0) { GameBoy.waitFrame(); }
        int value;
        if (kind == 0) {
            byte[] selected = scenario == 2 ? null : scenario == 1 ? EMPTY : BYTES;
            value = RomArrayReaders.byteAt(selected, index);
        } else if (kind == 1) {
            value = RomArrayReaders.shortAt(scenario == 2 ? null : SHORTS, index);
        } else if (kind == 2) {
            value = RomArrayReaders.charAt(scenario == 2 ? null : CHARS, index);
        } else if (kind == 3) {
            value = RomArrayReaders.intAt(scenario == 2 ? null : INTS, index);
        } else if (kind == 4) {
            value = RomArrayReaders.flagAt(scenario == 2 ? null : FLAGS, index) ? 1 : 0;
        } else {
            value = FIXED[index];
        }
        GameBoy.report(value);
    }
}

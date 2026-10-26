package probe;

public final class RomArrayEarlyNull {
    static int early = RomArrayEarlyNull.DATA[0];
    @RomData(bank = 0) static final byte[] DATA = {1};
    public static void main(String[] args) { GameBoy.report(early); }
}

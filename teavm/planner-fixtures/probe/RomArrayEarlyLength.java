package probe;

public final class RomArrayEarlyLength {
    static int early = RomArrayEarlyLength.DATA.length;
    @RomData(bank = 0) static final byte[] DATA = {1};
    public static void main(String[] args) { GameBoy.report(early); }
}

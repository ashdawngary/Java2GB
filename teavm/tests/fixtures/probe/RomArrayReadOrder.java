package probe;
import dev.java2gb.api.*;

public final class RomArrayReadOrder {
    @RomData(bank=2) static final byte[] FIRST={127,-128};
    @RomData(bank=3) static final byte[] SECOND={11,12};
    static byte[] selected;
    public static int scenario, index, effects;
    @NoInline static byte[] source() { effects=effects*10+1;return selected; }
    @NoInline static int offset() {
        effects=effects*10+2;
        if (scenario==3) selected=SECOND;
        return index;
    }
    @NoInline static int read() { return source()[offset()] & 255; }
    public static void main(String[] args) {
        while (GameBoy.buttons()==0) GameBoy.waitFrame();
        selected=scenario==2 ? null : FIRST;
        final int value=read();
        effects=effects*10+3;
        GameBoy.report(value);
    }
}

package probe;
import dev.java2gb.api.*;

import java.util.Objects;

public final class RomArrayRequireNonNull {
    @RomData(bank=2) static final byte[] DATA={127,-128};
    public static int scenario,index,effects;
    @NoInline static int offset() {effects=effects*10+1;return index;}
    @NoInline static int read(byte[] source,int at) {
        final byte[] checked=Objects.requireNonNull(source);
        effects=effects*10+2;
        return checked[at]&255;
    }
    @NoInline static int checkOnly(byte[] source,int at) {
        Objects.requireNonNull(source);
        effects=effects*10+2;
        return 777;
    }
    public static void main(String[] args) {
        while(GameBoy.buttons()==0)GameBoy.waitFrame();
        final byte[] source=scenario==2 || scenario==5?null:DATA;
        final int value=scenario>=4?checkOnly(source,offset()):read(source,offset());
        effects=effects*10+3;
        GameBoy.report(value);
    }
}

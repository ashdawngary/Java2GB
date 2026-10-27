package probe;
import dev.java2gb.api.*;

public final class RomArrayEarlyObject {
    @RomData(bank=0) static final byte[] DATA={1};
    static final Box SELF=new Box();
    static final class Box {
        int value;
        Box() {value=SELF.value;}
    }
    public static void main(String[] args) {GameBoy.report(SELF.value+DATA[0]);}
}

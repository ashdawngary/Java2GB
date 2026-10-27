package probe;
import dev.java2gb.api.*;
public final class UnsafeStringConstruction {
    public static void main(String[] args) { GameBoy.report(new String("DYNAMIC").length()); }
}

package probe;
import dev.java2gb.api.*;
public final class StaticStringNegative {
    public static void main(String[] args) { GameBoy.report("ABC".charAt(-1)); }
}

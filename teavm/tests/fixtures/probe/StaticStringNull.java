package probe;
import dev.java2gb.api.*;
public final class StaticStringNull {
    static String missing;
    public static void main(String[] args) { GameBoy.report(missing.length()); }
}

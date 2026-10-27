package probe;
import dev.java2gb.api.*;
public final class UnsafeStringConcat {
    public static void main(String[] args) { GameBoy.report(("SCORE " + GameBoy.buttons()).length()); }
}

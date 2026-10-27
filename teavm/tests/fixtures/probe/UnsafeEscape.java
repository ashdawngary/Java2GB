package probe;
import dev.java2gb.api.*;
public final class UnsafeEscape {
    static PlannedProbe.Cell last;
    @NoInline static void retain(PlannedProbe.Cell cell) { last = cell; }
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) retain(new PlannedProbe.Cell(i));
        GameBoy.report(last.value);
    }
}

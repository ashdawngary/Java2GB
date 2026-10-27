package probe;
import dev.java2gb.api.*;
public final class UnsafeOverlap {
    public static void main(String[] args) {
        PlannedProbe.Cell previous = null;
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            PlannedProbe.Cell next = new PlannedProbe.Cell(i);
            if (previous != null) sum += PlannedProbe.sample(previous);
            previous = next;
        }
        GameBoy.report(sum + PlannedProbe.sample(previous));
    }
}

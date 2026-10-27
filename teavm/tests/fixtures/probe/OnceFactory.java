package probe;
import dev.java2gb.api.*;
public final class OnceFactory {
    @NoInline static PlannedProbe.Cell create(int value) { return new PlannedProbe.Cell(value); }
    public static int compute() {
        PlannedProbe.Cell persistent = create(17);
        int sum = 0;
        for (int i = 0; i < 1000; i++) sum += MethodReuse.step(i);
        return sum + PlannedProbe.sample(persistent);
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

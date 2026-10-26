package probe;
public final class MethodReuse {
    @NoInline static int step(int value) {
        PlannedProbe.Cell temporary = new PlannedProbe.Cell(value);
        return PlannedProbe.sample(temporary);
    }
    public static int compute() {
        int sum = 0;
        for (int i = 0; i < 1000; i++) sum += step(i);
        return sum;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

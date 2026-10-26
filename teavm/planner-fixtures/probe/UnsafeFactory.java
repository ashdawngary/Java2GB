package probe;
public final class UnsafeFactory {
    @NoInline static PlannedProbe.Cell spawn(int value) { return new PlannedProbe.Cell(value); }
    public static void main(String[] args) {
        PlannedProbe.Cell first = spawn(11);
        PlannedProbe.Cell second = spawn(22);
        GameBoy.report(PlannedProbe.sample(first) + PlannedProbe.sample(second));
    }
}

package probe;
import dev.java2gb.api.*;

public final class PlannedProbe {
    static final class Cell {
        int value;
        int untouched;
        Cell(int value) { this.value = value; }
    }
    static Cell spawn(int value) { return new Cell(value); }
    @NoInline static Cell identity(Cell cell) { return cell; }
    @NoInline static int sample(Cell cell) {
        int result = cell.value + cell.untouched;
        cell.untouched = 9;
        return result;
    }
    @NoInline static int arraySum(int[] array) { return array[0] + array[3]; }
    @Mutable
    public static int compute() {
        Cell first = identity(spawn(11));
        Cell second = identity(spawn(22));
        int sum = first == second ? -100000 : sample(first) + sample(second);
        for (int i = 0; i < 20000; i++) {
            Cell temporary = new Cell(i);
            sum += sample(temporary);
            int[] scratch = new int[4];
            scratch[0] = i;
            sum += arraySum(scratch);  
            scratch[3] = 99;
        }
        return sum;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

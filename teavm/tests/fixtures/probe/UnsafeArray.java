package probe;
import dev.java2gb.api.*;
public final class UnsafeArray {
    public static void main(String[] args) {
        int[] data = new int[GameBoy.buttons()];
        GameBoy.report(PlannedProbe.arraySum(data));
    }
}

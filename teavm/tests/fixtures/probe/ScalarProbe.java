package probe;
import dev.java2gb.api.*;
public final class ScalarProbe {
    static final class Point { int x; int y; Point(int x, int y) { this.x = x; this.y = y; } }
    public static int compute() {
        Point point = new Point(10, 20);
        return point.x * point.x + point.y * point.y;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

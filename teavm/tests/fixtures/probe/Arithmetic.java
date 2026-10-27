package probe;
import dev.java2gb.api.*;

public final class Arithmetic {
    static int seed = 0x76543210;
    static int count;
    static int next() {
        seed ^= seed << 13;
        seed ^= seed >>> 17;
        seed ^= seed << 5;
        return seed;
    }
    static int operation(int op, int a, int b) {
        if (op == 0) return a + b;
        if (op == 1) return a - b;
        if (op == 2) return a * b;
        if (op == 3) return a / b;
        if (op == 4) return a % b;
        if (op == 5) return -a;
        if (op == 6) return a << b;
        if (op == 7) return a >> b;
        if (op == 8) return a >>> b;
        if (op == 9) return a & b;
        if (op == 10) return a | b;
        if (op == 11) return a ^ b;
        if (op == 12) return (byte)a;
        if (op == 13) return (short)a;
        if (op == 14) return (char)a;
        if (op == 15) return a < b ? 101 : -101;
        if (op == 16) return a <= b ? 202 : -202;
        if (op == 17) return a > b ? 303 : -303;
        if (op == 18) return a >= b ? 404 : -404;
        if (op == 19) return a == b ? 505 : -505;
        if (op == 20) return a != b ? 606 : -606;
        return 0;
    }
    public static int compute() {
        int hash = 17;
        for (int i = 0; i < 210; i++) {
            int a = next();
            int b = next();
            if (i % 7 == 0) a = -2147483648;
            if (i % 7 == 1) a = 2147483647;
            if (i % 7 == 2) a = 0;
            if (i % 7 == 3) b = a;
            if (i % 7 == 4) b = -1;
            if (i % 7 == 5) b = -32;
            if (b == 0) b = 1;
            for (int op = 0; op < 21; op++) {
                hash = hash * 31 + operation(op, a, b);
                count++;
            }
        }
        hash = hash * 31 + operation(3, -2147483648, -1);
        hash = hash * 31 + operation(4, -2147483648, -1);
        return hash ^ count;
    }
    public static void main(String[] args) {
        GameBoy.init();
        GameBoy.report(compute());
        while (true) GameBoy.waitFrame();
    }
}

package probe;
import dev.java2gb.api.*;
public final class PoolConstruction {
    @PoolCapacity(2) static final class Child { int value; Child(int value) { this.value = value; } }
    @PoolCapacity(2) static final class Parent { int marker; Child child; }
    @NoInline static Child child(int value) { return new Child(value); }
    @NoInline static Parent make(int value) {
        Parent parent = new Parent();
        parent.marker = 17;
        parent.child = child(value);
        return parent;
    }
    @NoInline static int read(Parent parent) { return parent.marker + parent.child.value; }
    public static int compute() {
        int sum = 0;
        for (int i = 0; i < 1000; i++) sum += read(make(i));
        return sum;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

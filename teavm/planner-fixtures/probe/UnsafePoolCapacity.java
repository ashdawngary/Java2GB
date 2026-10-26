package probe;
public final class UnsafePoolCapacity {
    @PoolCapacity(0) static final class Node { int value; }
    @NoInline static int read(Node n) { return n.value; }
    public static void main(String[] args) { GameBoy.report(read(new Node())); }
}

package probe;
public final class PoolExhaustion {
    @PoolCapacity(2) static final class Node { int value; Node next; Node(int value) { this.value = value; } }
    static Node root;
    @NoInline static Node make(int value) { return new Node(value); }
    @NoInline static int read(Node node) { return node.value; }
    public static void main(String[] args) {
        root = make(17); root.next = make(29);
        GameBoy.report(read(root) + read(root.next));
        root.next.next = make(99);  
        GameBoy.report(-1);
    }
}

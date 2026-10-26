package probe;
public final class PoolOwner {
    @PoolCapacity(3) static final class Node {
        int value;
        Node next;
        Node(int value) { this.value = value; }
    }
    static final class Holder { Node child; int marker = 11; }
    static Holder owner;
    static Node pinned;
    @NoInline static Node make(int value) { return new Node(value); }
    @NoInline static int read(Holder holder) { return holder.marker + holder.child.value; }
    public static int compute() {
        owner = new Holder(); owner.child = make(1); pinned = owner.child;
        int sum = 0;
        for (int i = 0; i < 500; i++) {
            owner.child = make(i + 2);
            Node scratch = make(7);
            sum += read(owner) + pinned.value + scratch.value;
            pinned = owner.child;
        }
        owner.child = null; pinned = null;
        Node fresh = make(23);
        return sum + fresh.value + (fresh.next == null ? 1000 : -10000);
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

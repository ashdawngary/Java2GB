package probe;
import dev.java2gb.api.*;
public final class PoolProbe {
    @PoolCapacity(4) static final class Node {
        Node next, alias;
        int value, zero;
        Node(int value) { this.value = value; }
    }
    static Node root;
    @NoInline static Node make(int value) { return new Node(value); }
    @NoInline static int nested(Node retained, int value) {
        Node scratch = make(value);
        return retained.value + scratch.value + scratch.zero + (scratch.next == null ? 1 : -10000);
    }
    @NoInline static int value(Node node) { return node.value; }
    public static int compute() {
        int sum = 0;
        root = make(3);
        root.next = make(5);
        root.alias = root.next;
        root.next.next = root;
        for (int i = 0; i < 500; i++) {
            Node held = make(i);
            held.zero = 99;
            sum += nested(held, i + 1);
            sum += value(root.alias) + (root.next.next == root ? 7 : -10000);
        }
        root = null;  
        for (int i = 0; i < 500; i++) {
            Node head = make(i);
            head.next = make(i + 1);
            head.next.next = make(i + 2);
            head.next.next.next = head;
            sum += head.value + head.next.value + head.next.next.value;
        }
        return sum;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

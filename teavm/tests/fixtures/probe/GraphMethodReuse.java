package probe;
import dev.java2gb.api.*;
public final class GraphMethodReuse {
    static final class Node { Node next; int value; Node(int value) { this.value = value; } }
    @NoInline static int read(Node node) { return node.value + node.next.value; }
    @NoInline static int step(int value) {
        Node first = new Node(value), second = new Node(3);
        first.next = second; second.next = first;
        return read(first) + read(second);
    }
    public static int compute() {
        int result = 0;
        for (int i = 0; i < 1000; i++) result += step(i);
        return result;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

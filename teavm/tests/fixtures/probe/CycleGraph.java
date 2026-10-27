package probe;
import dev.java2gb.api.*;
public final class CycleGraph {
    static final class Node { Node next; int value; Node(int value) { this.value = value; } }
    @NoInline static int value(Node node) { return node.value; }
    public static int compute() {
        Node first = new Node(3), second = new Node(7);
        first.next = second; second.next = first;
        return value(first) + value(first.next) + value(first.next.next) + (first.next.next == first ? 100 : -100);
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

package probe;
import dev.java2gb.api.*;
public final class UnsafeGraph {
    static final class Node { Node next; int value; }
    public static void main(String[] args) {
        Node a = new Node(); a.next = new Node();
        GameBoy.report(a.next == a ? 0 : 1);
    }
}

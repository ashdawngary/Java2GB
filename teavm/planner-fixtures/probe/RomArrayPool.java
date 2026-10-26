package probe;

public final class RomArrayPool {
    @RomData static final byte[] DATA = {-128, 0, 127};
    @PoolCapacity(3) static final class Cell {
        byte[] values;
        String label;
        int value;
        Cell(int value) { this.values = DATA; this.label = "ROM"; this.value = value; }
    }
    static Cell root;
    @NoInline static Cell create(int i) { return new Cell(i); }
    public static int run() {
        int hash = 1;
        for (int i = 0; i < 300; i++) {
            Cell previous = root;
            root = create(i);
            hash = hash * 31 + root.value + RomArrayReaders.byteAt(root.values, i % 3) + root.label.charAt(1);
            if (previous != null) { hash += previous.value; }
        }
        return hash;
    }
    public static void main(String[] args) { GameBoy.report(run()); }
}

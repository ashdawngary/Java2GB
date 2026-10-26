package probe;
public final class UnsafeRecursion {
    @NoInline static int recursive(int n) { return n == 0 ? 0 : 1 + recursive(n - 1); }
    public static void main(String[] args) { GameBoy.report(recursive(GameBoy.buttons())); }
}

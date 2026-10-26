package probe;
public final class UnsafeStringTypeCheck {
    static final class Holder {}
    @NoInline static Holder cast(Object value) { return (Holder)value; }
    public static void main(String[] args) { GameBoy.report(cast("TEXT") == null ? 1 : 0); }
}

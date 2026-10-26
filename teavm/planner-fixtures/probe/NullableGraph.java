package probe;
public final class NullableGraph {
    static final class Weapon { int damage = 10; }
    static final class Player { private final Weapon weapon; Player(boolean enabled) { weapon = enabled ? new Weapon() : null; } }
    @NoInline static boolean enabled(int value) { return value != 0; }
    @NoInline static int damage(Player player) { return player.weapon == null ? 3 : player.weapon.damage; }
    public static int compute() { return damage(new Player(enabled(0))) * 100 + damage(new Player(enabled(1))); }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

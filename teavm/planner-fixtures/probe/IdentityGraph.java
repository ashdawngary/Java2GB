package probe;
public final class IdentityGraph {
    static final class Weapon { int damage = 10; }
    static final class Player { int health = 100; private final Weapon weapon = new Weapon(); }
    @NoInline static boolean same(Weapon first, Weapon second) { return first == second; }
    @NoInline static int health(Player player) { return player.health; }
    public static int compute() {
        Player player = new Player();
        Weapon other = new Weapon();
        return health(player) + (same(player.weapon, player.weapon) ? 100 : -100) + (same(player.weapon, other) ? -1000 : 10);
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

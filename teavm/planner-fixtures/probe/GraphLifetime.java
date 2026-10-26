package probe;
public final class GraphLifetime {
    static final class Weapon { int damage; Weapon(int damage) { this.damage = damage; } }
    static final class Player { Weapon weapon; Player() { weapon = new Weapon(7); } }
    @NoInline static int damage(Player player) { return player.weapon.damage; }
    @NoInline static int temporary(Weapon weapon) { return weapon.damage; }
    public static int compute() {
        Player player = new Player();
        Weapon scratch = new Weapon(99);
        return temporary(scratch) + damage(player);
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

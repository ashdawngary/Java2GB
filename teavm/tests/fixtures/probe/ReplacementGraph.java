package probe;
import dev.java2gb.api.*;
public final class ReplacementGraph {
    static final class Weapon { int damage; Weapon(int damage) { this.damage = damage; } }
    static final class Player { int health = 100; Weapon weapon; Player() { weapon = new Weapon(7); } }
    @NoInline static int damage(Weapon weapon) { return weapon.damage; }
    @NoInline static int health(Player player) { return player.health; }
    public static int compute() {
        Player player = new Player();
        Weapon original = player.weapon;
        player.weapon = new Weapon(19);
        original.damage += 2;
        return health(player) + damage(original) * 100 + damage(player.weapon) + (original == player.weapon ? -10000 : 0);
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

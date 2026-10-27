package probe;
import dev.java2gb.api.*;
public final class OwnedGraph {
    static final class Weapon { int damage; Weapon(int damage) { this.damage = damage; } }
    static final class Player {
        private final Weapon weapon;
        int health = 100;
        Player(int damage) { weapon = new Weapon(damage); }
    }
    @NoInline static int health(Player player) { return player.health; }
    public static int compute() {
        Player first = new Player(10);
        Player second = new Player(20);
        first.weapon.damage += 5;
        return health(first) + health(second) + first.weapon.damage * 10 + second.weapon.damage;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

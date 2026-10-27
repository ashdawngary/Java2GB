package probe;
import dev.java2gb.api.*;
public final class StaticAliasGraph {
    static final class Weapon { int damage = 10; }
    static final class Player { int health = 100; private final Weapon weapon = new Weapon(); }
    static Weapon cached;
    @NoInline static int health(Player player) { return player.health; }
    public static int compute() {
        Player player = new Player();
        cached = player.weapon;
        cached.damage += 3;
        return health(player) + player.weapon.damage + (cached == player.weapon ? 1000 : -1000);
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

package probe;
import dev.java2gb.api.*;
public final class OwnedMethodReuse {
    static final class Weapon { int damage; int untouched; Weapon(int damage) { this.damage = damage; } }
    static final class Player { private final Weapon weapon; int health = 100; Player(int damage) { weapon = new Weapon(damage); } }
    @NoInline static int health(Player player) { return player.health; }
    @NoInline static int step(int damage) {
        Player player = new Player(damage);
        int result = health(player) + player.weapon.damage + player.weapon.untouched;
        player.weapon.untouched = 99;
        return result;
    }
    public static int compute() {
        int result = 0;
        for (int i = 0; i < 1000; i++) result += step(i);
        return result;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

package probe;
import dev.java2gb.api.*;
public final class UnsafeEquip {
    static final class Weapon { int damage; Weapon(int damage) { this.damage = damage; } }
    static final class Player { Weapon weapon; }
    @NoInline static void equip(Player player, int damage) { player.weapon = new Weapon(damage); }
    @NoInline static int damage(Player player) { return player.weapon.damage; }
    public static void main(String[] args) {
        Player player = new Player();
        equip(player, 3); equip(player, 7);
        GameBoy.report(damage(player));
    }
}

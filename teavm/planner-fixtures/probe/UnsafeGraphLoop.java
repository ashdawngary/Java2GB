package probe;
public final class UnsafeGraphLoop {
    static final class Weapon { int damage; Weapon(int damage) { this.damage = damage; } }
    static final class Player { Weapon weapon; }
    @NoInline static int damage(Player player) { return player.weapon.damage; }
    public static void main(String[] args) {
        Player player = new Player();
        for (int i = 0; i < 10; i++) { player.weapon = new Weapon(i); GameBoy.report(damage(player)); }
    }
}

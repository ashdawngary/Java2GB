package probe;
public final class SharedGraph {
    static final class Weapon { int damage = 7; }
    static final class Player { private final Weapon weapon; Player(Weapon weapon) { this.weapon = weapon; } }
    @NoInline static int damage(Player player) { return player.weapon.damage; }
    public static int compute() {
        Weapon shared = new Weapon();
        Player first = new Player(shared), second = new Player(shared);
        first.weapon.damage += 2;
        return damage(first) + damage(second) + (first.weapon == second.weapon ? 100 : -100);
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

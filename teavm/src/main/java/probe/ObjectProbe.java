package probe;

public final class ObjectProbe {
    @Mutable
    public static int compute() {
        Player player = new Player(76, 68);
        Player other = new Player(10, 20);
        int[] inputs = { GameBoy.RIGHT, GameBoy.DOWN | GameBoy.A, GameBoy.LEFT, GameBoy.UP };
        for (int input : inputs) player.update(input);
        other.update(GameBoy.RIGHT | GameBoy.A);
        return player.position() ^ other.position();
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

package probe;
import dev.java2gb.api.*;

public final class OwnedSpriteGame {
    static final class Weapon { int speed = 1; }
    static final class Player {
        private final Weapon weapon = new Weapon();
        int x = 76;
        int y = 68;
    }
    static final class Game {
        private final Player player = new Player();
        int marker;
    }
    @NoInline static int marker(Game game) { return game.marker; }
    static int clamp(int value, int max) {
        if (value < 0) return 0;
        if (value > max) return max;
        return value;
    }
    public static void main(String[] args) {
        Game game = new Game();
        GameBoy.init();
        for (int row = 0; row < 8; row++) {
            int bits = row == 0 || row == 7 ? 0x18
                : row == 1 || row == 6 ? 0x3c
                : row == 2 || row == 5 ? 0x7e : 0xff;
            GameBoy.tileRow(row, bits + marker(game), bits);
        }
        GameBoy.moveSprite(game.player.x, game.player.y);
        GameBoy.show();
        while (true) {
            GameBoy.waitFrame();
            int buttons = GameBoy.buttons();
            int speed = game.player.weapon.speed;
            if ((buttons & GameBoy.A) != 0) speed *= 2;
            if ((buttons & GameBoy.RIGHT) != 0) game.player.x += speed;
            if ((buttons & GameBoy.LEFT) != 0) game.player.x -= speed;
            if ((buttons & GameBoy.DOWN) != 0) game.player.y += speed;
            if ((buttons & GameBoy.UP) != 0) game.player.y -= speed;
            if ((buttons & GameBoy.START) != 0) { game.player.x = 76; game.player.y = 68; }
            game.player.x = clamp(game.player.x, 152);
            game.player.y = clamp(game.player.y, 136);
            GameBoy.moveSprite(game.player.x, game.player.y);
        }
    }
}

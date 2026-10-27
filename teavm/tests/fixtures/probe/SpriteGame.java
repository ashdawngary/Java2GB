package probe;
import dev.java2gb.api.*;

public final class SpriteGame {
    public static void main(String[] args) {
        GameBoy.init();
        for (int row = 0; row < 8; row++) {
            int bits = (row == 0 || row == 7) ? 0x18
                : (row == 1 || row == 6) ? 0x3c
                : (row == 2 || row == 5) ? 0x7e : 0xff;
            GameBoy.tileRow(row, bits, bits);
        }
        Player player = new Player(76, 68);
        player.draw();
        GameBoy.show();
        while (true) {
            GameBoy.waitFrame();
            player.update(GameBoy.buttons());
            player.draw();
        }
    }
}

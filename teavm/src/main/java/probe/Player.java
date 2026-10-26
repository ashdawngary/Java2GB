package probe;

public final class Player {
    private int x;
    private int y;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update(int buttons) {
        int speed = (buttons & GameBoy.A) != 0 ? 2 : 1;
        if ((buttons & GameBoy.RIGHT) != 0) x += speed;
        if ((buttons & GameBoy.LEFT) != 0) x -= speed;
        if ((buttons & GameBoy.DOWN) != 0) y += speed;
        if ((buttons & GameBoy.UP) != 0) y -= speed;
        if ((buttons & GameBoy.START) != 0) { x = 76; y = 68; }
        x = clamp(x, 152);
        y = clamp(y, 136);
    }

    private int clamp(int value, int max) {
        if (value < 0) return 0;
        if (value > max) return max;
        return value;
    }

    public void draw() { GameBoy.moveSprite(x, y); }
    public int position() { return y * 256 + x; }
}

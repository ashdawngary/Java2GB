package probe;
public final class PinballCamera {
    public static int compute() {
        PinballGame.Table table = new PinballGame.Table();
        PinballGame.Camera camera = table.camera;
        int checks = 0;
        if (camera.y == 128 && table.ball.y == 236 << 4) checks |= 1;
        int previous = camera.y;
        camera.follow(26, PinballGame.PLAY);
        if (camera.y == previous - 6) checks |= 2;
        for (int i = 0; i < 30; i++) camera.follow(26, PinballGame.PLAY);
        if (camera.y == 0) checks |= 4;
        camera.y = 64; camera.follow(128, PinballGame.PLAY);
        if (camera.y == 64) checks |= 8;
        for (int i = 0; i < 30; i++) camera.follow(250, PinballGame.PLAY);
        if (camera.y == 128) checks |= 16;
        camera.follow(26, PinballGame.PLAY);
        if (camera.y == 122 && table.ball.y == 236 << 4) checks |= 32;
        camera.follow(26, PinballGame.READY);
        if (camera.y == 128) checks |= 64;
        table.step(GameBoy.DOWN);
        int minimum = 128, maxChange = 0;
        for (int i = 0; i < 100; i++) {
            previous = camera.y; table.step(0);
            int change = PinballGame.abs(camera.y - previous);
            if (change > maxChange) maxChange = change;
            if (camera.y < minimum) minimum = camera.y;
        }
        if (minimum == 0 && !table.ball.lane && maxChange <= 6) checks |= 128;
        return checks;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

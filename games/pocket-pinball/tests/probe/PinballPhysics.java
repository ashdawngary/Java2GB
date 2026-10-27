package probe;
import dev.java2gb.api.*;
public final class PinballPhysics {
    static void place(PinballGame.Table t, int x, int y, int vx, int vy) {
        t.state = PinballGame.PLAY; t.ball.lane = false;
        t.ball.gravityPhase = 1;
        t.ball.x = x << 4; t.ball.y = y << 4; t.ball.vx = vx; t.ball.vy = vy;
    }
    public static int compute() {
        PinballGame.Table t = new PinballGame.Table();
        int checks = 0;
        t.step(GameBoy.A);
        for (int i = 0; i < 8; i++) t.step(0);
        if (t.state == PinballGame.PLAY && t.launches == 1 && t.ball.vy < 0) checks |= 1;
        place(t, 18, 40, -32, 0); t.step(0);
        if (t.ball.vx > 0 && t.ball.x == 18 << 4) checks |= 2;
        place(t, 40, 46, 0, 24); t.step(0);
        if (t.lights == 1 && t.score == 100 && t.ball.vy < 0) checks |= 4;
        place(t, 80, 78, 0, 24); t.step(0);
        place(t, 120, 46, 0, 24); t.step(0);
        if (t.lights == 7 && t.bumperHits == 3 && t.score == 300) checks |= 8;
        place(t, 80, 32, 0, 0); t.step(0);
        if (t.state == PinballGame.PLAY && t.bonusHits == 1 && t.score == 1300) checks |= 16;
        t.step(0);
        if (t.state == PinballGame.PLAY && t.lights == 0 && t.ball.y > 44 << 4) checks |= 32;
        place(t, 60, 224, 0, 32); t.step(GameBoy.LEFT);
        if (t.flipperHits == 1 && t.ball.vy == -64 && t.ball.vx > 0) checks |= 64;
        t.step(0); place(t, 100, 224, 0, 32); t.step(GameBoy.A);
        if (t.flipperHits == 2 && t.ball.vy == -64 && t.ball.vx < 0) checks |= 128;
        place(t, 80, 110, 0, 0); t.step(GameBoy.UP); t.step(GameBoy.UP);
        if (t.nudges == 1 && t.ball.vy < 0) checks |= 2048;
        place(t, 80, 260, 0, 32); t.step(0);
        if (t.lives == 2 && t.drains == 1 && t.state == PinballGame.READY) checks |= 256;
        place(t, 80, 260, 0, 32); t.step(0);
        place(t, 80, 260, 0, 32); t.step(0);
        if (t.state == PinballGame.OVER && t.lives == 0 && t.drains == 3) checks |= 512;
        t.step(GameBoy.START);
        if (t.state == PinballGame.READY && t.lives == 3 && t.score == 0 && t.bonusHits == 0) checks |= 1024;
        return checks;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

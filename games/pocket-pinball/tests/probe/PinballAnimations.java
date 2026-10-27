package probe;
import dev.java2gb.api.*;
public final class PinballAnimations {
    public static int compute() {
        PinballGame.Table table = new PinballGame.Table();
        int checks = 0;
        table.step(0);
        if (table.state == PinballGame.READY && table.plunger == 0 && table.ball.spin == 0
                && table.ball.y == 236 << 4) checks |= 1;
        table.step(GameBoy.DOWN);
        if (table.state == PinballGame.READY && table.plunger == 8 && table.launches == 0) checks |= 2;
        int poses = 0;
        boolean still = true;
        for (int i = 0; i < 7; i++) {
            table.step(0);
            poses |= 1 << (((table.ball.y >> 4) - 236) >> 1);
            if (table.state != PinballGame.READY || table.ball.spin != 0) still = false;
        }
        if (poses == 15 && still) checks |= 4;
        table.step(0);
        if (table.state == PinballGame.PLAY && table.plunger == 0 && table.launches == 1
                && table.ball.vy == -92 && table.ball.y == 236 << 4) checks |= 8;
        table.step(0);
        if (table.ball.spin == 91 && table.ball.y < 236 << 4) checks |= 16;
        int turns = 0;
        for (int i = 0; i < 16; i++) { table.step(0); turns |= 1 << ((table.ball.spin >> 7) & 3); }
        if (turns == 15) checks |= 32;
        PinballPhysics.place(table,80,116,0,4); table.ball.spin = 0; table.step(0);
        int slow = table.ball.spin;
        PinballPhysics.place(table,80,116,0,40); table.ball.spin = 0; table.step(0);
        if (slow == 5 && table.ball.spin == 40) checks |= 64;
        table.ball.lane = true; table.ball.x = 148 << 4; table.ball.y = 40 << 4;
        table.ball.vx = table.ball.vy = table.ball.spin = 0; table.gate = table.gateClock = 0;
        table.step(0);
        if (table.ball.spin == 0 && table.ball.x == 148 << 4) checks |= 128;
        table.step(GameBoy.START);
        if (table.state == PinballGame.READY && table.ball.spin == 0 && table.plunger == 0) checks |= 256;
        table.state = PinballGame.OVER; table.ball.spin = 99;
        table.step(0);
        if (table.ball.spin == 99 && table.state == PinballGame.OVER) checks |= 512;
        return checks;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

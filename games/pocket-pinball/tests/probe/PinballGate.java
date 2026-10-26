package probe;
public final class PinballGate {
    public static int compute() {
        PinballGame.Table table = new PinballGame.Table();
        int checks = 0;
        if (table.gate == 0 && table.ball.lane && table.ball.x == 148 << 4) checks |= 1;
        table.step(GameBoy.DOWN);
        int opened = 1, closing = 0, crossing = 0, closedAt = -1, releasedAt = -1;
        boolean continuous = true, clearance = true, closesBehind = true;
        for (int i = 0; i < 100; i++) {
            int oldX = table.ball.x, oldGate = table.gate;
            boolean wasLane = table.ball.lane;
            table.step(0);
            if (wasLane) {
                opened |= 1 << table.gate;
                if (PinballGame.abs(table.ball.x-oldX) > 24) continuous = false;
                if (table.ball.x < oldX) crossing++;
                if (table.gate < oldGate) closesBehind = false;
                if (table.ball.x < (148 << 4) && table.ball.x > (132 << 4)) {
                    if (table.gate != 3 || table.ball.y < (28 << 4) || table.ball.y > (44 << 4)) clearance = false;
                }
                if (!table.ball.lane) {
                    releasedAt = i;
                    if (table.ball.x + (4 << 4) > (136 << 4) || table.gate != 3) clearance = false;
                }
            } else if (releasedAt >= 0 && closedAt < 0) {
                closing |= 1 << table.gate;
                if (table.gate == 0) closedAt = i;
            }
        }
        if (opened == 15) checks |= 2;
        if (continuous && crossing >= 11) checks |= 4;
        if (clearance && releasedAt >= 0) checks |= 8;
        if (closesBehind && closing == 15) checks |= 16;
        if (closedAt > releasedAt && closedAt <= releasedAt + 9) checks |= 32;
        table.reset(); table.state = PinballGame.PLAY;
        table.ball.y = 40 << 4;
        boolean waits = true;
        for (int i = 0; i < 8; i++) {
            table.step(0);
            if (table.ball.x != 148 << 4 || !table.ball.lane || table.gate == 3) waits = false;
        }
        if (waits && table.gate == 2) checks |= 64;
        table.step(0); table.step(0);
        if (table.gate == 3 && table.ball.x == (148 << 4) - 24) checks |= 128;
        table.step(GameBoy.START);
        if (table.gate == 0 && table.gateClock == 0 && table.state == PinballGame.READY) checks |= 256;
        table.step(GameBoy.DOWN);
        for (int i = 0; i < 8; i++) table.step(0);
        table.step(GameBoy.UP);
        if (table.nudges == 0 && table.ball.x == 148 << 4 && table.ball.y < 236 << 4) checks |= 512;
        return checks;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

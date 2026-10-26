package probe;
public final class PinballReplay {
    public static int compute() {
        PinballGame.Table table = new PinballGame.Table();
        int checksum = 0;
        for (int i = 0; i < 1800; i++) {
            PinballGame.frames = i;
            int buttons = 0;
            if ((i & 32) == 0) buttons |= GameBoy.B;
            if ((i & 64) == 0) buttons |= GameBoy.A;
            if ((i & 127) == 0) buttons |= GameBoy.UP;
            if (table.state == PinballGame.READY) buttons |= GameBoy.DOWN;
            if (table.state == PinballGame.OVER) buttons |= GameBoy.START;
            table.step(buttons);
            checksum = (checksum * 31) + table.ball.x + table.ball.y + table.score + table.state;
        }
        return checksum;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

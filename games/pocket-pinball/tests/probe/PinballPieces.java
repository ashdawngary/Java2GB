package probe;
public final class PinballPieces {
    static void place(PinballGame.Table table, int x, int y, int vx, int vy) {
        PinballPhysics.place(table, x, y, vx, vy);
    }
    public static int compute() {
        PinballGame.Table table = new PinballGame.Table();
        int checks = 0;
        place(table,56,124,0,24); table.step(0);
        if (table.upperTarget.down == 1 && table.targetMask == 1 && table.targetHits == 1
                && table.score == 150 && table.ball.vy < 0) checks |= 1;
        place(table,56,124,0,24); table.step(0);
        if (table.targetHits == 1 && table.score == 150 && table.ball.vy > 0) checks |= 2;
        place(table,104,172,0,-24); table.step(0);
        if (table.lowerTarget.down == 1 && table.targetMask == 3 && table.targetBanks == 1
                && table.score == 800 && table.targetResetClock == 90) checks |= 4;
        if (table.ball.vy > 0 && table.ball.y == 173 << 4) checks |= 8;
        for (int i = 0; i < 89; i++) { place(table,80,116,0,0); table.step(0); }
        if (table.targetResetClock == 1 && table.upperTarget.down == 1 && table.lowerTarget.down == 1) checks |= 16;
        place(table,80,116,0,0); table.step(0);
        if (table.targetResetClock == 0 && table.targetMask == 0
                && table.upperTarget.down == 0 && table.lowerTarget.down == 0) checks |= 32;
        place(table,44,132,32,0); table.step(0);
        if (table.upperTarget.down == 1 && table.ball.vx < 0 && table.ball.x == 43 << 4) checks |= 64;
        place(table,32,184,0,24); table.step(0);
        if (table.kickerHits == 1 && table.score == 1025 && table.ball.vx == 28
                && table.ball.vy == -48 && table.leftKicker.cooldown == 12) checks |= 128;
        place(table,120,184,0,24); table.step(0);
        if (table.kickerHits == 2 && table.score == 1100 && table.ball.vx == -28
                && table.ball.vy == -48 && table.rightKicker.cooldown == 12) checks |= 256;
        place(table,32,184,0,24); table.step(0);
        if (table.kickerHits == 2 && table.score == 1100 && table.ball.vy < 0) checks |= 512;
        for (int i = 0; i < 10; i++) { place(table,80,116,0,0); table.step(0); }
        place(table,32,184,0,24); table.step(0);
        if (table.kickerHits == 3 && table.score == 1175 && table.ball.vy == -48) checks |= 1024;
        place(table,80,192,0,32); table.step(0);
        if (table.ball.vx == 0 && table.ball.vy == 33 && (table.ball.y >> 4) == 194) checks |= 2048;
        place(table,80,260,0,32); table.step(0);
        if (table.state == PinballGame.READY && table.lives == 2 && table.targetMask == 0
                && table.upperTarget.down == 0 && table.lowerTarget.down == 0
                && table.leftKicker.cooldown == 0 && table.rightKicker.cooldown == 0) checks |= 4096;
        table.step(GameBoy.START);
        if (table.lives == 3 && table.score == 0 && table.targetHits == 0
                && table.targetBanks == 0 && table.kickerHits == 0) checks |= 8192;
        place(table,56,120,0,0); table.step(0);
        if (table.targetHits == 0 && table.upperTarget.down == 0) checks |= 16384;
        return checks;
    }
    public static void main(String[] args) { GameBoy.report(compute()); }
}

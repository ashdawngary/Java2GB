package probe;

final class PinballTitle {
    private PinballTitle() {}
    static void line(int y, String text) {
        for (int column = 0; column < 20; column++)
            GameBoy.background(column, y, GameBoy.textTile(text, column, true));
    }
    static void label(int x, int y, String text) {
        int columns = (GameBoy.textWidth(text) + 7) >> 3;
        for (int i = 0; i < columns; i++)
            GameBoy.background(x + i, y, GameBoy.textTile(text, i, false));
    }
    static void number(int x, int y, int value) {
        int divisor = 10000;
        for (int i = 0; i < 5; i++) {
            int digit = 0;
            while (value >= divisor) { value -= divisor; digit++; }
            GameBoy.background(x + i, y, 32 + digit);
            divisor /= 10;
        }
    }
    @NoInline static void show(PinballScores scores, int lastScore) {
        GameBoy.init(); GameBoy.hud(144); GameBoy.scroll(0, 0);
        line(1, "POCKET"); line(2, "PINBALL"); line(4, "HIGH SCORES");
        for (int i = 0; i < 5; i++) {
            GameBoy.background(5, 6 + i, 33 + i);
            number(9, 6 + i, scores.get(i));
        }
        if (lastScore > 0) { label(4, 12, "LAST SCORE"); number(10, 12, lastScore > 99999 ? 99999 : lastScore); }
        line(14, "PRESS START");
        line(16, "LEFT RIGHT FLIPPERS"); line(17, "DOWN LAUNCH UP NUDGE");
        GameBoy.display();
    }
}

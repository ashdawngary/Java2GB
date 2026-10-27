package probe;
import dev.java2gb.api.*;

public final class TetrisChecks {
  public static final class CharacterState {
    char first, second;
  }

  static CharacterState narrow;

  @NoInline
  static int narrowFields(final CharacterState state, final int value) {
    state.first = (char) value;
    state.second = (char) (value + 1);
    return state.first * 31 + state.second;
  }

  static TetrisRules g;
  static int count, failures, hash = 17;

  static void check(final boolean ok) {
    count++;
    if (!ok) {
      failures++;
    }
    hash = hash * 31 + (ok ? count : -count * 97);
  }

  @Mutable
  static void empty() {
    for (int i = 0; i < g.board.length; i++) {
      g.board[i] = 0;
    }
  }

  @Mutable
  static void row(final int y) {
    for (int x = 0; x < 10; x++) {
      g.board[y * 10 + x] = (byte) 132;
    }
  }

  @Mutable
  @NoInline
  public static int run() {
    g = new TetrisRules();
    narrow = new CharacterState();
    count = 0;
    failures = 0;
    hash = 17;
    check(narrowFields(narrow, -1) == 65535 * 31);
    check(narrowFields(narrow, 65536) == 1);
    check(narrowFields(narrow, 0x18000) == 32768 * 31 + 32769);
    g.reset(0, 0, 0, 1234);
    check(g.lines == 0 && g.level == 0 && g.phase == TetrisRules.FALL);
    for (int shape = 0; shape < 28; shape++) {
      check(g.fits(shape, 3, 4));
      check(!g.fits(shape, -4, 4));
      check(!g.fits(shape, 10, 4));
      check(!g.fits(shape, 3, 18));
      int packed = TetrisPieces.cells(shape), mask = 0;
      for (int i = 0; i < 4; i++) {
        mask |= 1 << (packed & 15);
        packed >>>= 4;
      }
      int blocks = 0;
      for (int i = 0; i < 16; i++) {
        blocks += (mask >>> i) & 1;
      }
      check(blocks == 4);
    }
    g.piece = 0;
    g.x = 3;
    g.y = 4;
    g.gravity = 52;
    g.tick(GameBoy.A);
    check(g.piece == 3);
    g.tick(0);
    g.tick(GameBoy.B);
    check(g.piece == 0);
    int oldY = g.y;
    g.tick(GameBoy.UP);
    check(g.y == oldY);
    g.reset(0, 0, 0, 1);
    g.piece = 8;
    g.tick(GameBoy.A);
    check(g.piece == 11 && g.phase == TetrisRules.FALL);
    g.piece = 9;
    g.lock();
    check((g.board[194] & 255) == 128 && !g.fits(9, 3, 1));
    g.reset(0, 0, 0, 1);
    check(g.fits(9, 3, 1) && g.board[194] == 0);
    g.reset(0, 0, 0, 1234);
    oldY = g.y;
    for (int i = 0; i < 52; i++) {
      g.tick(0);
    }
    check(g.y == oldY);
    g.tick(0);
    check(g.y == oldY + 1);
    g.reset(0, 0, 0, 1);
    g.piece = 12;
    g.y = 4;
    g.tick(GameBoy.RIGHT);
    final int oldX = g.x;
    for (int i = 0; i < 22; i++) {
      g.tick(GameBoy.RIGHT);
    }
    check(g.x == oldX);
    g.tick(GameBoy.RIGHT);
    check(g.x == oldX + 1);
    for (int i = 0; i < 8; i++) {
      g.tick(GameBoy.RIGHT);
    }
    check(g.x == oldX + 1);
    g.tick(GameBoy.RIGHT);
    check(g.x == oldX + 2);
    g.reset(0, 0, 0, 1);
    oldY = g.y;
    g.tick(GameBoy.DOWN);
    check(g.y == oldY + 1);
    g.tick(GameBoy.DOWN);
    g.tick(GameBoy.DOWN);
    check(g.y == oldY + 1);
    g.tick(GameBoy.DOWN);
    check(g.y == oldY + 2);
    oldY = g.y;
    g.tick(GameBoy.DOWN | GameBoy.RIGHT);
    check(g.y == oldY && g.softSteps == 0);
    for (int n = 1; n <= 4; n++) {
      g.reset(0, 0, 0, 3);
      empty();
      for (int y = 18 - n; y < 18; y++) {
        row(y);
      }
      g.findLines();
      check(g.cleared == n && g.lines == n);
      g.collapse();
      check(g.score == (n == 1 ? 40 : n == 2 ? 100 : n == 3 ? 300 : 1200));
      int sum = 0;
      for (int i = 0; i < 180; i++) {
        sum += g.board[i];
      }
      check(sum == 0);
    }
    g.reset(0, 0, 0, 3);
    row(0);
    row(1);
    g.findLines();
    check(g.cleared == 0);
    empty();
    g.board[0] = (byte) 132;
    row(16);
    row(17);
    g.findLines();
    g.collapse();
    check(g.board[0] == 0 && (g.board[10] & 255) == 132 && (g.board[20] & 255) == 132);
    g.reset(0, 0, 0, 3);
    g.lines = 9;
    row(17);
    g.findLines();
    g.collapse();
    check(g.level == 1 && g.score == 40);
    g.reset(0, 9, 0, 3);
    g.lines = 89;
    row(17);
    g.findLines();
    check(g.level == 9);
    g.lines = 99;
    g.findLines();
    g.collapse();
    check(g.level == 10 && g.score == 400);
    g.reset(0, 20, 0, 3);
    g.lines = 9998;
    row(16);
    row(17);
    g.findLines();
    g.collapse();
    check(g.lines == 9999 && g.level == 20 && g.score == 2100);
    g.reset(1, 5, 5, 3);
    int filled = 0;
    for (int y = 0; y < 18; y++) {
      int holes = 0;
      for (int x = 0; x < 10; x++) {
        if (g.board[y * 10 + x] == 0) {
          holes++;
        } else {
          filled++;
        }
      }
      check(y < 8 ? holes == 10 : holes > 0);
    }
    check(filled > 0 && g.lines == 25 && g.level == 5);
    g.softSteps = 5;
    g.piece = 9;
    g.y = 16;
    g.lock();
    check(g.score == 0 && g.softPoints == 4);
    g.softPoints = 65534;
    g.softSteps = 5;
    g.lock();
    check(g.softPoints == 2 && g.score == 0);
    empty();
    g.lines = 1;
    row(17);
    g.findLines();
    g.collapse();
    g.timer = 0;
    g.tick(0);
    check(g.lines == 0 && g.phase == TetrisRules.WON);
    g.reset(0, 0, 0, 3);
    row(1);
    row(2);
    g.spawn();
    check(g.phase == TetrisRules.OVER);
    g.reset(0, 0, 0, 3);
    g.score = 999990;
    g.addScore(1200);
    check(g.score == 999999);
    g.reset(0, 4, 0, 8491);
    for (int frame = 0; frame < 3600; frame++) {
      int b = (frame % 150 < 70 ? GameBoy.LEFT : GameBoy.RIGHT);
      if (frame % 31 == 0) {
        b |= GameBoy.A;
      }
      if (frame % 4 != 0) {
        b = GameBoy.DOWN;
      }
      if (g.phase == TetrisRules.OVER) {
        g.reset(0, 4, 0, 8491 + frame);
      }
      g.tick(b);
      hash = hash * 31 + g.x + g.y * 11 + g.piece * 197 + g.score;
    }
    for (int i = 0; i < 180; i++) {
      hash = hash * 31 + (g.board[i] & 255);
    }
    return hash;
  }

  public static void main(final String[] args) {
    GameBoy.report(run());
  }
}

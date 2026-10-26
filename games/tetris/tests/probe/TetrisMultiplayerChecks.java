package probe;

public final class TetrisMultiplayerChecks {
  public static int count;

  private static void check(final boolean condition) {
    count++;
    if (!condition) {
      throw new AssertionError(count);
    }
  }

  public static void main(final String[] args) {
    run();
    System.out.println(count);
  }

  @Mutable
  public static void run() {
    for (int entropy = 1; entropy < 1000; entropy += 137) {
      final TetrisRules template = new TetrisRules();
      template.sequence = new byte[256];
      final byte[] garbage = new byte[100];
      for (int i = 0; i < 256; i++) {
        template.sequence[i] = (byte) (((i + entropy) % 7) * 4);
      }
      for (int i = 0; i < 100; i++) {
        garbage[i] = (byte) (i % 3 == 0 ? 47 : 128 + i % 8);
      }
      template.loadMatch(5, garbage);
      check(template.mode == 2 && template.level == 1 && template.lines == 30);
      for (int height = 0; height <= 5; height++) {
        final TetrisRules g = new TetrisRules();
        g.sequence = new byte[256];
        for (int i = 0; i < 256; i++) {
          g.sequence[i] = template.sequence[i];
        }
        g.loadMatch(height, garbage);
        check(g.piece == template.piece && g.next == template.next && g.hidden == template.hidden);
        check(g.garbageHole == template.garbageHole && g.garbageHole >= 0 && g.garbageHole < 10);
        for (int i = 0; i < 256; i++) {
          check(g.sequence[i] == template.sequence[i]);
          check(g.sequence[i] >= 0 && g.sequence[i] <= 24 && (g.sequence[i] & 3) == 0);
        }
        for (int row = 0; row < 18; row++) {
          for (int col = 0; col < 10; col++) {
            check(g.board[row * 10 + col] == (row < 18 - height * 2
                ? 0 : template.board[(row - (10 - height * 2)) * 10 + col]));
          }
        }
        for (int i = 0; i < 520; i++) {
          g.spawn();
          check(g.piece == g.sequence[(i + 1) & 255]);
        }
      }
    }
    for (int count = 1; count <= 18; count++) {
      for (int hole = 0; hole < 10; hole++) {
        final TetrisRules g = new TetrisRules();
        g.reset(0, 0, 0, 1);
        g.mode = 2;
        g.garbageHole = (byte) hole;
        for (int i = 0; i < 180; i++) {
          g.board[i] = (byte) (128 + (i % 7));
        }
        for (int i = 180; i < 200; i++) {
          g.board[i] = (byte) (128 + (i % 7));
        }
        g.phase = TetrisRules.ENTRY;
        g.timer = 2;
        g.lines = 30;
        g.receiveGarbage(count);
        for (int step = 0; step < 9; step++) {
          g.tick(0);
          check(g.phase == TetrisRules.ENTRY);
        }
        check(g.garbageCursor == -1 && g.garbageRows == 0 && g.garbageRedraw);
        for (int i = 180; i < 200; i++) {
          check((g.board[i] & 255) == 128 + (i % 7));
        }
        for (int i = 0; i < 180; i++) {
          check((g.board[i] & 255) == (i < (18 - count) * 10
              ? 128 + ((i + count * 10) % 7) : i % 10 == hole ? 0 : 40));
        }
      }
    }
    for (int clear = 0; clear <= 4; clear++) {
      check(TetrisRules.attack(clear) == (clear < 2 ? 0 : clear == 4 ? 4 : clear - 1));
    }
    for (int clear = 1; clear <= 4; clear++) {
      for (int top = 0; top < 18 - clear; top++) {
        final TetrisRules g = new TetrisRules();
        g.reset(0, 0, 0, 1);
        g.mode = 2;
        g.lines = 30;
        g.occupiedTop = (byte) top;
        g.board[top * 10 + 9] = (byte) 128;
        for (int row = 18 - clear; row < 18; row++) {
          for (int column = 0; column < 10; column++) { g.board[row * 10 + column] = (byte) 130; }
        }
        g.phase = TetrisRules.LOCK;
        g.timer = 1;
        for (int frame = 0; frame < 200 && g.phase != TetrisRules.ENTRY; frame++) {
          g.tick(0);
          if (g.phase == TetrisRules.SETTLE) { g.stackHeight(); }
        }
        final int expectedTop = top == 0 ? 1 : top + clear;
        check(g.phase == TetrisRules.ENTRY);
        check(g.occupiedTop == expectedTop);
        check((g.board[expectedTop * 10 + 9] & 255) == 128);
        check(g.stackHeight() == 18 - expectedTop);
      }
    }
  }
}

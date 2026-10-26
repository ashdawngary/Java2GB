package probe;

import java.io.DataInputStream;
import java.io.FileInputStream;

public final class TetrisOriginalReplay {
  public static void main(final String[] args) throws Exception {
    final TetrisRules game = new TetrisRules();
    int compared = 0;
    try (final DataInputStream input = new DataInputStream(new FileInputStream(args[0]))) {
      for (int shape = 0; shape < 28; shape += 4) {
        game.reset(0, 0, 0, 1);
        game.piece = (byte) shape;
        game.x = 3;
        game.y = 3;
        for (int frame = 0; frame < 140; frame++) {
          final int buttons = input.readUnsignedByte();
          final int expectedPiece = input.readUnsignedByte();
          final int expectedX = input.readByte();
          final int expectedY = input.readByte();
          game.tick(buttons);
          if (game.piece != expectedPiece || game.x != expectedX || game.y != expectedY) {
            throw new AssertionError(
                "Shape "
                    + shape
                    + ", frame "
                    + frame
                    + ": Java "
                    + game.piece
                    + ","
                    + game.x
                    + ","
                    + game.y
                    + "; original "
                    + expectedPiece
                    + ","
                    + expectedX
                    + ","
                    + expectedY);
          }
          compared++;
        }
      }
    }
    System.out.println(compared);
  }
}

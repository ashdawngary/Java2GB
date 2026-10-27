package probe;
import dev.java2gb.api.*;

import static probe.TetrisLayout.*;

public final class TetrisRenderChecks {
  private static final int BACKGROUND = 0x9800, MAP_COLUMNS = 32;
  private TetrisRenderChecks() {}

  private static void checkBoard(final TetrisRules game, final int test) {
    for (int row = 0; row < FIELD_ROWS; row++) {
      for (int column = 0; column < FIELD_COLUMNS; column++) {
        final int cell = row * FIELD_COLUMNS + column;
        final int value = game.board[cell] & BYTE_MASK;
        final int expected = value == 0 ? EMPTY_TILE : value;
        final int actual = GameBoyHardware.read(BACKGROUND + row * MAP_COLUMNS + FIELD_TILE_X + column);
        if (actual != expected) {
          GameBoy.report(-test * VISIBLE_CELLS - cell - 1);
          while (true) { GameBoy.waitFrame(); }
        }
      }
    }
  }

  public static void main(final String[] args) {
    final TetrisRules game = TetrisGame.game;
    final TetrisRender video = TetrisGame.video;
    int tests = 0;
    for (int mode = MODE_A; mode <= MODE_LINK; mode++) {
      for (int shape = 0; shape < TetrisRules.PIECE_TYPES * TetrisRules.ORIENTATIONS; shape++) {
        for (int position = 0; position < 3; position++) {
          game.reset(mode == MODE_LINK ? MODE_A : mode, 0, 0, 1);
          game.mode = (byte) mode;
          if (mode == MODE_LINK) { game.lines = TetrisProtocol.OBJECTIVE_LINES; }
          game.sequence = TetrisGame.link.pieces;
          game.piece = (byte) shape;
          game.next = (byte) ((shape + TetrisRules.ORIENTATIONS) % (TetrisRules.PIECE_TYPES * TetrisRules.ORIENTATIONS));
          game.hidden = (byte) ((shape + 2 * TetrisRules.ORIENTATIONS) % (TetrisRules.PIECE_TYPES * TetrisRules.ORIENTATIONS));
          video.screen(mode == MODE_LINK ? LINK_GAME_SCREEN : mode == MODE_B ? GAME_B_SCREEN : GAME_A_SCREEN);
          for (int row = 0; row < FIELD_ROWS; row++) { video.row(game, row, false); }
          video.game(game, true);
          game.x = (byte) (position * 3);
          while (game.fits(game.piece, game.x, game.y + 1)) { game.y++; }
          game.lock();
          while (game.phase != TetrisRules.FALL) { game.tick(0); }
          video.game(game, true);
          checkBoard(game, tests);
          tests++;
        }
      }
    }
    GameBoy.report(tests);
    while (true) { GameBoy.waitFrame(); }
  }
}

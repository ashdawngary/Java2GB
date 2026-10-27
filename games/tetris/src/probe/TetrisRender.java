package probe;
import dev.java2gb.api.*;

import static probe.TetrisLayout.*;
import static probe.TetrisRules.PIECE_BLOCKS;
import static probe.TetrisRules.CELL_BITS;
import static probe.TetrisRules.CELL_MASK;
import static probe.TetrisRules.CELL_COLUMN_MASK;
import static probe.TetrisRules.CELL_ROW_SHIFT;

public final class TetrisRender {
  @RomData(bank = 0)
  private static final int[] DECIMAL_PLACES = {1, 10, 100, 1000, 10000, 100000};
  private static final int FACE_COLUMNS = 2, FACE_LOWER_TILE_OFFSET = 1;
  private static final int BACKGROUND = 0x9800, MAP_COLUMNS = 32;

  @NoInline
  public void face(final int first, final int x, final int y, final int tile) {
    TetrisHardware.sprite(first, x, y, tile, 0);
    TetrisHardware.sprite(first + 1, x + TILE_PIXELS, y, tile, SPRITE_MIRROR_X);
    TetrisHardware.sprite(first + FACE_COLUMNS, x, y + TILE_PIXELS, tile + FACE_LOWER_TILE_OFFSET, 0);
    TetrisHardware.sprite(first + FACE_COLUMNS + 1, x + TILE_PIXELS, y + TILE_PIXELS,
        tile + FACE_LOWER_TILE_OFFSET, SPRITE_MIRROR_X);
  }

  public byte redrawRow = -1, lastPhase = -1, lastFlash = -1, lastLevel = -1;
  private byte flashRow = -1;
  public boolean garbageWipe;
  private char garbageClock;
  private final byte[] garbageTiles = new byte[VISIBLE_CELLS];
  public int lastLocks = -1, lastScore = -1;
  public short lastLines = -1;
  public byte lastPiece = -1, lastNext = -1, lastX, lastY;
  public boolean lastVisible, lastPreview;
  byte dx0, dy0, dx1, dy1, dx2, dy2, dx3, dy3;

  public void text(final int x, final int y, final byte[] value) {
    for (int i = 0; i < value.length; i++) {
      final int c = value[i] & BYTE_MASK;
      int tile = EMPTY_TILE;
      if (c >= 'A' && c <= 'Z') {
        tile = c - 'A' + LETTER_A_TILE;
      } else if (c >= '0' && c <= '9') {
        tile = c - '0';
      } else if (c == '-') {
        tile = HYPHEN_TILE;
      }
      GameBoy.background(x + i, y, tile);
    }
  }

  @NoInline
  public void label(final int first, final byte[] text, final int x, final int y,
      final boolean selected, final int frames) {
    final boolean blink = !selected || ((frames >>> MENU_BLINK_SHIFT) & 1) == 0;
    for (int i = 0; i < text.length; i++) {
      final int c = text[i] & BYTE_MASK, tile = c == ' ' ? EMPTY_TILE : c == '-' ? HYPHEN_TILE : c - 'A' + LETTER_A_TILE;
      TetrisHardware.sprite(first + i, x + i * TILE_PIXELS, blink ? y : HIDDEN_SPRITE_Y, tile, 0);
    }
  }

  @NoInline
  public void screen(final int id) {
    TetrisHardware.lcd(TetrisHardware.LCD_OFF);
    GameBoy.hideSprites(0, SPRITE_COUNT);
    TetrisHardware.tiles(id == TITLE_SCREEN ? 0 : 1);
    for (int row = 0; row < SCREEN_ROWS; row++) {
      for (int col = 0; col < SCREEN_COLUMNS; col++) {
        GameBoy.background(col, row, TetrisData.map(id, row * SCREEN_COLUMNS + col));
      }
    }
    lastLocks = -1;
    lastPhase = -1;
    lastFlash = -1;
    lastScore = -1;
    lastLines = -1;
    lastLevel = -1;
    redrawRow = -1;
    flashRow = -1;
    garbageWipe = false;
    lastPiece = -1;
    lastNext = -1;
    lastPreview = false;
    lastVisible = false;
  }

  public void show() {
    TetrisHardware.lcd(TetrisHardware.LCD_ON);
  }

  public boolean pendingRows() {
    return redrawRow >= 0 || flashRow >= 0;
  }

  @Mutable
  @NoInline
  public void prepareGarbage(final TetrisRules g, final int firstRow, final int endRow) {
    final byte[] cells = g.board;
    for (short cell = (short) (firstRow * FIELD_COLUMNS); cell < endRow * FIELD_COLUMNS; cell++) {
      final byte value = cells[cell];
      garbageTiles[cell] = value == 0 ? (byte) EMPTY_TILE : value;
    }
  }

  @NoInline
  public void wipeGarbage(final TetrisRules g) {
    final char now = (char) TetrisHardware.clock();
    if (g.garbageRedraw) {
      g.garbageRedraw = false;
      garbageWipe = true;
      redrawRow = FIELD_ROWS - 1;
      garbageClock = (char) (now - 1);
    }
    if (now == garbageClock) { return; }
    garbageClock = now;
    RomMemory.copyToVram(garbageTiles, redrawRow * FIELD_COLUMNS,
        BACKGROUND + redrawRow * MAP_COLUMNS + FIELD_TILE_X, 0, FIELD_COLUMNS);
    if (--redrawRow < 0) { garbageWipe = false; }
  }

  @NoInline
  public void number(final int x, final int y, final int width, int value) {
    boolean started = false;
    for (int i = 0; i < width; i++) {
      final int place = DECIMAL_PLACES[width - i - 1];
      int digit = 0;
      while (value >= place) {
        value -= place;
        digit++;
      }
      if (digit > 0 || i == width - 1) {
        started = true;
      }
      GameBoy.background(x + i, y, started ? digit : EMPTY_TILE);
    }
  }

  @NoInline
  public void piece(final int first, final int shape, final int px, final int py, final boolean visible) {
    int cells = TetrisPieces.cells(shape);
    if (first == PIECE_SLOT) {
      dx0 = (byte) ((cells & CELL_COLUMN_MASK) * TILE_PIXELS);
      dy0 = (byte) ((((cells >>> (0 * CELL_BITS + CELL_ROW_SHIFT)) & CELL_COLUMN_MASK) - HIDDEN_ROWS) * TILE_PIXELS);
      dx1 = (byte) (((cells >>> (1 * CELL_BITS)) & CELL_COLUMN_MASK) * TILE_PIXELS);
      dy1 = (byte) ((((cells >>> (1 * CELL_BITS + CELL_ROW_SHIFT)) & CELL_COLUMN_MASK) - HIDDEN_ROWS) * TILE_PIXELS);
      dx2 = (byte) (((cells >>> (2 * CELL_BITS)) & CELL_COLUMN_MASK) * TILE_PIXELS);
      dy2 = (byte) ((((cells >>> (2 * CELL_BITS + CELL_ROW_SHIFT)) & CELL_COLUMN_MASK) - HIDDEN_ROWS) * TILE_PIXELS);
      dx3 = (byte) (((cells >>> (3 * CELL_BITS)) & CELL_COLUMN_MASK) * TILE_PIXELS);
      dy3 = (byte) ((((cells >>> (3 * CELL_BITS + CELL_ROW_SHIFT)) & CELL_COLUMN_MASK) - HIDDEN_ROWS) * TILE_PIXELS);
    }
    for (int block = 0; block < PIECE_BLOCKS; block++) {
      final int cell = cells & CELL_MASK;
      cells >>>= CELL_BITS;
      TetrisHardware.sprite(
          first + block,
          px + (cell & CELL_COLUMN_MASK) * TILE_PIXELS,
          visible ? py + ((cell >>> CELL_ROW_SHIFT) - HIDDEN_ROWS) * TILE_PIXELS : HIDDEN_SPRITE_Y,
          TetrisPieces.tile(shape, block),
          SPRITE_BEHIND_BACKGROUND);
    }
  }

  @NoInline
  public void hud(final TetrisRules g) {
    if (lastScore != g.score && g.mode == MODE_A) {
      number(SCORE_X, SCORE_Y, SCORE_DIGITS, g.score);
      lastScore = g.score;
    }
    if (lastLines != g.lines) {
      number(g.mode == MODE_LINK ? LINK_LINES_X : SINGLE_LINES_X, LINES_Y,
          g.mode == MODE_LINK ? LINK_LINES_DIGITS : SINGLE_LINES_DIGITS, g.lines);
      lastLines = g.lines;
    }
    if (lastLevel != g.level && g.mode != MODE_LINK) {
      number(LEVEL_X, g.mode == MODE_A ? LEVEL_A_Y : LEVEL_B_Y,
          g.mode == MODE_A ? LEVEL_A_DIGITS : LEVEL_B_DIGITS, g.level);
      lastLevel = g.level;
    }
  }

  private static int tile(final byte[] cells, final int index) {
    final int value = cells[index] & BYTE_MASK;
    return value == 0 ? EMPTY_TILE : value;
  }

  private static int packedTiles(final byte[] cells, final int first) {
    return tile(cells, first) | (tile(cells, first + 1) << 8)
        | (tile(cells, first + 2) << 16) | (tile(cells, first + 3) << 24);
  }

  @NoInline
  public void row(final TetrisRules g, final int y, final boolean flash) {
    if (flash && (g.clearMask & (1 << y)) != 0
        && ((g.clearFrame & 1) != 0 || g.clearFrame == TetrisRules.CLEAR_FLASH_STEPS)) {
      TetrisHardware.backgroundRun(FIELD_TILE_X, y, FIELD_COLUMNS, g.clearFrame == TetrisRules.CLEAR_FLASH_STEPS ? EMPTY_TILE : FLASH_TILE);
      return;
    }
    final int first = y * FIELD_COLUMNS;
    final byte[] cells = g.board;
    TetrisHardware.backgroundPacked(FIELD_TILE_X, y, 4, packedTiles(cells, first));
    TetrisHardware.backgroundPacked(FIELD_TILE_X + 4, y, 4, packedTiles(cells, first + 4));
    TetrisHardware.backgroundPacked(FIELD_TILE_X + 8, y, 2, tile(cells, first + 8) | (tile(cells, first + 9) << 8));
  }

  @NoInline
  public void game(final TetrisRules g, final boolean preview) {
    if (g.garbageRedraw || garbageWipe) {
      wipeGarbage(g);
      return;
    }
    if (g.phase != TetrisRules.CLEAR) {
      flashRow = -1;
    }
    if (g.locks != lastLocks) {
      if (lastLocks >= 0) {
        final int shape = g.lockedPiece, px = g.lockedX, py = g.lockedY;
        int cells = TetrisPieces.cells(shape);
        for (int i = 0; i < PIECE_BLOCKS; i++) {
          final int cell = cells & CELL_MASK;
          cells >>>= CELL_BITS;
          GameBoy.background(
              FIELD_TILE_X + px + (cell & CELL_COLUMN_MASK), (py + (cell >>> CELL_ROW_SHIFT) - HIDDEN_ROWS) & 31, TetrisPieces.tile(shape, i));
        }
      }
      lastLocks = g.locks;
    }
    if (g.phase == TetrisRules.CLEAR && g.clearFrame != lastFlash) {
      flashRow = -1;
      if ((g.clearFrame & 1) != 0) {
        for (int y = 2; y < FIELD_ROWS; y++) {
          if ((g.clearMask & (1 << y)) != 0) {
            row(g, y, true);
          }
        }
      } else if (g.clearFrame != 0) {
        flashRow = 2;
      }
      lastFlash = g.clearFrame;
    }
    if (g.phase == TetrisRules.CLEAR && flashRow >= 0) {
      int y = flashRow;
      while (y < FIELD_ROWS && (g.clearMask & (1 << y)) == 0) {
        y++;
      }
      if (y < FIELD_ROWS) {
        row(g, y, true);
        flashRow = (byte) (y + 1);
      } else {
        flashRow = -1;
      }
    }
    if (g.phase == TetrisRules.SETTLE && lastPhase != g.phase) {
      for (int y = 2; y < FIELD_ROWS; y++) {
        if ((g.clearMask & (1 << y)) != 0) {
          TetrisHardware.backgroundRun(FIELD_TILE_X, y, FIELD_COLUMNS, EMPTY_TILE);
        }
      }
    }
    if (g.phase == TetrisRules.ENTRY && g.cleared != 0 && lastPhase != TetrisRules.ENTRY) {
      redrawRow = g.redrawTop < FIELD_ROWS ? g.redrawTop : (byte) -1;
    }
    if (redrawRow >= 0) {
      row(g, redrawRow, false);
      if (++redrawRow == FIELD_ROWS) {
        redrawRow = -1;
      }
    }
    final boolean visible = g.phase == TetrisRules.FALL;
    if (lastPiece != g.piece || lastVisible != visible) {
      piece(0, g.piece, FIELD_PIXEL_X + g.x * TILE_PIXELS, g.y * TILE_PIXELS, visible);
      lastPiece = g.piece;
      lastX = g.x;
      lastY = g.y;
      lastVisible = visible;
    } else if (visible && (lastX != g.x || lastY != g.y)) {
      final int px = FIELD_PIXEL_X + g.x * TILE_PIXELS, py = g.y * TILE_PIXELS;
      GameBoy.sprite(0, px + dx0, py + dy0);
      GameBoy.sprite(1, px + dx1, py + dy1);
      GameBoy.sprite(2, px + dx2, py + dy2);
      GameBoy.sprite(3, px + dx3, py + dy3);
      lastX = g.x;
      lastY = g.y;
    }
    if (lastNext != g.next || lastPreview != preview) {
      piece(PREVIEW_SLOT, g.next, PREVIEW_X, PREVIEW_Y, preview);
      lastNext = g.next;
      lastPreview = preview;
    }
    hud(g);
    lastPhase = g.phase;
  }
}

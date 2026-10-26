package probe;

import static probe.TetrisLayout.*;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public final class TetrisRules {
  public static final int PIECE_TYPES = 7, ORIENTATIONS = 4, PIECE_BLOCKS = 4;
  public static final int ROTATION_MASK = ORIENTATIONS - 1, PIECE_TYPE_MASK = 0xfc;
  public static final int CELL_BITS = 4, CELL_MASK = 0x0f, CELL_COLUMN_MASK = 3, CELL_ROW_SHIFT = 2;
  public static final int RANDOM_ATTEMPTS = 3, MAX_CLEAR_ROWS = 4;
  private static final int SPAWN_X = 3, SPAWN_Y = 1;
  private static final int B_TYPE_LINES = 25, MAX_LINES = 9999, MAX_SCORE = 999999;
  private static final int MAX_LEVEL = 20, LINES_PER_LEVEL = 10;
  private static final int LOCK_DELAY = 1, EMPTY_ENTRY_DELAY = 2, ENTRY_DELAY = 18;
  public static final int CLEAR_FLASH_STEPS = 7;
  private static final int CLEAR_INITIAL_DELAY = 2, CLEAR_FLASH_DELAY = 10, SETTLE_DELAY = 13;
  private static final int SHIFT_INITIAL_DELAY = 23, SHIFT_REPEAT_DELAY = 9, SOFT_DROP_DELAY = 3;
  private static final int GARBAGE_ROWS_PER_UPDATE = 2;
  private static final int RANDOM_MULTIPLIER = 109, RANDOM_INCREMENT = 89;
  private static final int SINGLE_POINTS = 40, DOUBLE_POINTS = 100;
  private static final int TRIPLE_POINTS = 300, TETRIS_POINTS = 1200;
  private static final int MIN_ATTACK_CLEAR_ROWS = 2, TETRIS_ATTACK_ROWS = 4;
  @RomData(bank = 0)
  private static final byte[] DROP_FRAMES = {
    52, 48, 44, 40, 36, 32, 27, 21, 16, 10,
    9, 8, 7, 6, 5, 5, 4, 4, 3, 3
  };
  public static final int FALL = 0, LOCK = 1, CLEAR = 2, SETTLE = 3, ENTRY = 4, OVER = 5, WON = 6;
  public final byte[] board = new byte[BOARD_CELLS];
  public byte @Nullable [] sequence;
  public byte sequenceIndex;
  public byte garbagePending, garbageRows, garbageCursor = -1, garbageHole;
  public boolean garbageRedraw;
  public byte piece, next, hidden, phase, gravity, softTimer, softSteps;
  public byte lockedPiece, lockedX, lockedY;
  public byte x, y;
  public byte level, mode, height, cleared, clearFrame, previous, repeat = 1, sound;
  public boolean softBlocked;
  public char seed = 1;
  public char softPoints;
  public byte occupiedTop = FIELD_ROWS, redrawTop = FIELD_ROWS;
  private byte collapseSource = -1, collapseDestination = -1;
  public short lines;
  public int score, clearMask, locks, timer;

  public static byte speed(final int level) {
    if (level < 0 || level >= DROP_FRAMES.length) {
      return 2;
    }
    return DROP_FRAMES[level];
  }

  public int randomByte() {
    seed = (char) (seed * RANDOM_MULTIPLIER + RANDOM_INCREMENT);
    return seed >>> 8;
  }

  public byte candidate(final int current, final int lookahead) {
    int result = 0;
    for (int attempt = 0; attempt < RANDOM_ATTEMPTS; attempt++) {
      result = pieceFromRandom(randomByte());
      if (attempt == RANDOM_ATTEMPTS - 1 || candidateAllowed(result, current, lookahead)) {
        break;
      }
    }
    return (byte) result;
  }

  public static int pieceFromRandom(final int sample) {
    return (((sample + BYTE_MASK) & BYTE_MASK) % PIECE_TYPES) * ORIENTATIONS;
  }

  public static boolean candidateAllowed(final int candidate, final int current, final int lookahead) {
    return ((candidate | lookahead | current) & PIECE_TYPE_MASK) != current;
  }

  private static int boardIndex(final int row, final int column) {
    return row < 0 ? VISIBLE_CELLS + (row + HIDDEN_ROWS) * FIELD_COLUMNS + column
        : row * FIELD_COLUMNS + column;
  }

  @Mutable
  @NoInline
  public void reset(final int gameMode, final int selectedLevel, final int selectedHeight, final int entropy) {
    garbagePending = 0;
    garbageRows = 0;
    garbageCursor = -1;
    garbageRedraw = false;
    mode = (byte) gameMode;
    level = (byte) selectedLevel;
    height = (byte) selectedHeight;
    seed = (char) entropy;
    score = 0;
    lines = (short) (mode == MODE_A ? 0 : B_TYPE_LINES);
    previous = 0;
    repeat = 1;
    softBlocked = false;
    softTimer = 0;
    softSteps = 0;
    softPoints = 0;
    occupiedTop = (byte) (mode == MODE_A ? FIELD_ROWS : FIELD_ROWS - height * HEIGHT_ROWS);
    redrawTop = FIELD_ROWS;
    locks = 0;
    cleared = 0;
    clearMask = 0;
    clearFrame = 0;
    sound = TetrisSound.EFFECT_NONE;
    for (int i = 0; i < BOARD_CELLS; i++) {
      board[i] = 0;
    }
    if (mode != MODE_A) {
      for (int row = FIELD_ROWS - height * HEIGHT_ROWS; row < FIELD_ROWS; row++) {
        boolean hole = false;
        for (int col = 0; col < FIELD_COLUMNS; col++) {
          final int v = randomByte();
          if ((v & 1) == 0 || (col == FIELD_COLUMNS - 1 && !hole)) {
            board[row * FIELD_COLUMNS + col] = 0;
            hole = true;
          } else {
            board[row * FIELD_COLUMNS + col] = (byte) (FIRST_BLOCK_TILE + (randomByte() & BLOCK_VARIANT_MASK));
          }
        }
      }
    }
    next = candidate(0, 0);
    hidden = candidate(0, next);
    spawn();
  }

  @Mutable
  @NoInline
  public void loadMatch(final int selectedHeight, final byte[] garbage) {
    reset(MODE_A, TetrisProtocol.GRAVITY_LEVEL, 0, 1);
    mode = MODE_LINK;
    height = (byte) selectedHeight;
    lines = TetrisProtocol.OBJECTIVE_LINES;
    occupiedTop = FIELD_ROWS;
    for (int row = FIELD_ROWS - height * HEIGHT_ROWS; row < FIELD_ROWS; row++) {
      for (int col = 0; col < FIELD_COLUMNS; col++) {
        final int value = garbage[(row - (FIELD_ROWS - height * HEIGHT_ROWS)) * FIELD_COLUMNS + col] & BYTE_MASK;
        board[row * FIELD_COLUMNS + col] = (byte) (value == EMPTY_TILE ? 0 : value);
        if (value != EMPTY_TILE && row < occupiedTop) {
          occupiedTop = (byte) row;
        }
      }
    }
    final byte[] linkedPieces = Objects.requireNonNull(sequence);
    final int last = linkedPieces[TetrisProtocol.PIECE_SEQUENCE_LENGTH - 1] & BYTE_MASK;
    garbageHole = (byte) (((last == 0 ? TetrisProtocol.PIECE_SEQUENCE_LENGTH : last) - 1) % FIELD_COLUMNS);
    sequenceIndex = 2;
    next = linkedPieces[0];
    hidden = linkedPieces[1];
    spawn();
    sound = TetrisSound.EFFECT_NONE;
  }

  @NoInline
  public int stackHeight() {
    for (int row = occupiedTop; row < FIELD_ROWS; row++) {
      for (int col = 0; col < FIELD_COLUMNS; col++) {
        if (board[row * FIELD_COLUMNS + col] != 0) {
          occupiedTop = (byte) row;
          return FIELD_ROWS - row;
        }
      }
    }
    occupiedTop = FIELD_ROWS;
    return 0;
  }

  public static int attack(final int cleared) {
    return cleared < MIN_ATTACK_CLEAR_ROWS ? 0 : cleared == MAX_CLEAR_ROWS ? TETRIS_ATTACK_ROWS : cleared - 1;
  }

  private static int clearPoints(final int rows) {
    return rows == 1 ? SINGLE_POINTS : rows == 2 ? DOUBLE_POINTS : rows == 3 ? TRIPLE_POINTS : TETRIS_POINTS;
  }

  public void receiveGarbage(final int rows) {
    garbagePending += rows;
    if (garbagePending > FIELD_ROWS) {
      garbagePending = FIELD_ROWS;
    }
  }

  @Mutable
  @NoInline
  public void garbageStep() {
    if (garbageCursor < 0) {
      garbageRows = garbagePending;
      garbagePending = 0;
      garbageCursor = 0;
      occupiedTop = FIELD_ROWS;
    }
    for (int step = 0; step < GARBAGE_ROWS_PER_UPDATE && garbageCursor < FIELD_ROWS; step++) {
      final byte row = garbageCursor++;
      final short first = (short) (row * FIELD_COLUMNS);
      final short source = (short) (first + garbageRows * FIELD_COLUMNS);
      for (byte col = 0; col < FIELD_COLUMNS; col++) {
        final byte value = row + garbageRows < FIELD_ROWS
            ? board[source + col]
            : (byte) (col == garbageHole ? 0 : GARBAGE_TILE);
        board[first + col] = value;
        if (value != 0 && row < occupiedTop) {
          occupiedTop = (byte) row;
        }
      }
    }
    if (garbageCursor == FIELD_ROWS) {
      garbageCursor = -1;
      garbageRows = 0;
      garbageRedraw = true;
      redrawTop = 0;
      timer = ENTRY_DELAY;
      sound = TetrisSound.EFFECT_SETTLE;
    }
  }

  @NoInline
  public boolean fits(final int shape, final int px, final int py) {
    int cells = TetrisPieces.cells(shape);
    for (int i = 0; i < PIECE_BLOCKS; i++) {
      final int cell = cells & CELL_MASK;
      cells >>>= CELL_BITS;
      final int cx = px + (cell & CELL_COLUMN_MASK), cy = py + (cell >>> CELL_ROW_SHIFT) - HIDDEN_ROWS;
      if (cx < 0 || cx >= FIELD_COLUMNS || cy < -HIDDEN_ROWS || cy >= FIELD_ROWS) {
        return false;
      }
      final int index = boardIndex(cy, cx);
      if (board[index] != 0) {
        return false;
      }
    }
    return true;
  }

  @NoInline
  public void spawn() {
    piece = next;
    next = hidden;
    if (mode == MODE_LINK) {
      hidden = Objects.requireNonNull(sequence)[sequenceIndex & TetrisProtocol.PIECE_SEQUENCE_MASK];
      sequenceIndex++;
    } else {
      hidden = candidate(piece & PIECE_TYPE_MASK, next);
    }
    x = SPAWN_X;
    y = SPAWN_Y;
    gravity = speed(level);
    softTimer = 0;
    softSteps = 0;
    phase = FALL;
    clearMask = 0;
    cleared = 0;
    if (!fits(piece, x, y)) {
      phase = OVER;
      timer = 0;
      sound = TetrisSound.EFFECT_OVER;
    }
  }

  @Mutable
  @NoInline
  public void lock() {
    int cells = TetrisPieces.cells(piece);
    for (int i = 0; i < PIECE_BLOCKS; i++) {
      final int cell = cells & CELL_MASK;
      cells >>>= CELL_BITS;
      final int row = y + (cell >>> CELL_ROW_SHIFT) - HIDDEN_ROWS, column = x + (cell & CELL_COLUMN_MASK);
      final int index = boardIndex(row, column);
      board[index] = (byte) TetrisPieces.tile(piece, i);
      if (row >= 0 && row < occupiedTop) {
        occupiedTop = (byte) row;
      }
    }
    lockedPiece = piece;
    lockedX = x;
    lockedY = y;
    locks++;
    phase = LOCK;
    timer = LOCK_DELAY;
    softBlocked = true;
    sound = TetrisSound.EFFECT_LOCK;
    final int points = softSteps > 0 ? softSteps - 1 : 0;
    if (mode == MODE_A) {
      addScore(points);
    } else {
      softPoints = (char) (softPoints + points);
    }
    softSteps = 0;
  }

  public void addScore(final int amount) {
    score += amount;
    if (score > MAX_SCORE) {
      score = MAX_SCORE;
    }
  }

  @NoInline
  private static boolean fullRow(final byte[] cells, final int first) {
    return cells[first] != 0 && cells[first + 1] != 0 && cells[first + 2] != 0
        && cells[first + 3] != 0 && cells[first + 4] != 0 && cells[first + 5] != 0
        && cells[first + 6] != 0 && cells[first + 7] != 0 && cells[first + 8] != 0
        && cells[first + 9] != 0;
  }

  @NoInline
  public void findLines() {
    clearMask = 0;
    cleared = 0;
    for (int row = 2; row < FIELD_ROWS; row++) {
      if (fullRow(board, row * FIELD_COLUMNS)) {
        clearMask |= 1 << row;
        cleared++;
      }
    }
    if (cleared == 0) {
      phase = ENTRY;
      timer = EMPTY_ENTRY_DELAY;
      return;
    }
    if (mode == MODE_A) {
      lines += cleared;
      if (lines > MAX_LINES) {
        lines = MAX_LINES;
      }
    } else {
      lines -= cleared;
      if (lines < 0) {
        lines = 0;
      }
    }
    clearFrame = 0;
    timer = CLEAR_INITIAL_DELAY;
    phase = CLEAR;
    sound = (byte) (cleared == MAX_CLEAR_ROWS ? TetrisSound.EFFECT_TETRIS : TetrisSound.EFFECT_CLEAR);
  }

  @Mutable
  @NoInline
  public void collapse() {
    beginCollapse();
    while (collapseDestination >= 0) {
      collapseStep();
    }
    finishCollapse();
  }

  private void beginCollapse() {
    collapseSource = FIELD_ROWS - 1;
    collapseDestination = FIELD_ROWS - 1;
    redrawTop = occupiedTop;
  }

  @Mutable
  @NoInline
  private void collapseStep() {
    if (collapseDestination < 0) {
      return;
    }
    int source = collapseSource;
    if (source >= 0) {
      collapseSource--;
      if ((clearMask & (1 << source)) != 0) {
        return;
      }
    } else {
      source = 0;
    }
    final int destination = collapseDestination--;
    if (destination == 0) {
      for (int col = 0; col < FIELD_COLUMNS; col++) {
        board[col] = 0;
      }
    } else if (source != destination) {
      final int from = source * FIELD_COLUMNS, to = destination * FIELD_COLUMNS;
      for (int col = 0; col < FIELD_COLUMNS; col++) {
        board[to + col] = board[from + col];
      }
    }
  }

  @NoInline
  private void finishCollapse() {
    occupiedTop = (byte) (redrawTop == 0 ? 1 : redrawTop + cleared);
    if (occupiedTop > FIELD_ROWS) {
      occupiedTop = FIELD_ROWS;
    }
    if (mode == MODE_A) {
      final int points = clearPoints(cleared);
      addScore(points * (level + 1));
      if (level < MAX_LEVEL && lines >= (level + 1) * LINES_PER_LEVEL) {
        level++;
      }
    }
    phase = ENTRY;
    timer = ENTRY_DELAY;
    sound = TetrisSound.EFFECT_SETTLE;
  }

  @NoInline
  public void tick(final int buttons) {
    sound = TetrisSound.EFFECT_NONE;
    final int pressed = buttons & ~(previous & BYTE_MASK);
    previous = (byte) buttons;
    if (phase == OVER || phase == WON) {
      timer++;
      return;
    }
    if (mode == MODE_LINK && phase == ENTRY && lines != 0
        && (garbagePending > 0 || garbageCursor >= 0)) {
      garbageStep();
      return;
    }
    if (phase != FALL) {
      if (phase == SETTLE) {
        collapseStep();
        collapseStep();
      }
      if (timer > 0) {
        timer--;
      }
      if (timer != 0) {
        return;
      }
      if (phase == LOCK) {
        findLines();
      } else if (phase == CLEAR) {
        clearFrame++;
        if (clearFrame == CLEAR_FLASH_STEPS) {
          phase = SETTLE;
          timer = SETTLE_DELAY;
          beginCollapse();
        } else {
          timer = CLEAR_FLASH_DELAY;
        }
      } else if (phase == SETTLE) {
        finishCollapse();
      } else if (mode != MODE_A && lines == 0) {
        phase = WON;
        timer = 0;
        sound = TetrisSound.EFFECT_WIN;
      } else {
        spawn();
      }
      return;
    }
    int rotated = piece;
    if ((pressed & GameBoy.B) != 0) {
      rotated = (piece & PIECE_TYPE_MASK) | ((piece + 1) & ROTATION_MASK);
    } else if ((pressed & GameBoy.A) != 0) {
      rotated = (piece & PIECE_TYPE_MASK) | ((piece - 1) & ROTATION_MASK);
    }
    if (rotated != piece && fits(rotated, x, y)) {
      piece = (byte) rotated;
      sound = TetrisSound.EFFECT_ROTATE;
    }
    int direction = 0;
    if ((pressed & GameBoy.RIGHT) != 0) {
      direction = 1;
      repeat = SHIFT_INITIAL_DELAY;
    } else if ((buttons & GameBoy.RIGHT) != 0) {
      if (--repeat == 0) {
        direction = 1;
        repeat = SHIFT_REPEAT_DELAY;
      }
    } else if ((pressed & GameBoy.LEFT) != 0) {
      direction = -1;
      repeat = SHIFT_INITIAL_DELAY;
    } else if ((buttons & GameBoy.LEFT) != 0) {
      if (--repeat == 0) {
        direction = -1;
        repeat = SHIFT_REPEAT_DELAY;
      }
    } else {
      repeat = SHIFT_INITIAL_DELAY;
    }
    if (direction != 0) {
      if (fits(piece, x + direction, y)) {
        x += direction;
        sound = TetrisSound.EFFECT_MOVE;
      } else {
        repeat = 1;
      }
    }
    final boolean down = (buttons & (GameBoy.DOWN | GameBoy.LEFT | GameBoy.RIGHT)) == GameBoy.DOWN;
    if (softTimer > 0) {
      softTimer--;
    }
    if (down
        && softBlocked
        && (pressed & (GameBoy.DOWN | GameBoy.LEFT | GameBoy.RIGHT)) == GameBoy.DOWN) {
      softBlocked = false;
    }
    if (down && !softBlocked) {
      if (softTimer != 0) {
        return;
      }
      softTimer = SOFT_DROP_DELAY;
      softSteps++;
    } else {
      softSteps = 0;
      if (gravity > 0) {
        gravity--;
        return;
      }
      gravity = speed(level);
    }
    if (fits(piece, x, y + 1)) {
      y++;
    } else {
      lock();
    }
  }
}

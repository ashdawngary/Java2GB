package probe;

import static probe.PokemonConstants.*;

public final class PokemonHud {
  private static final int DIGITS = 13, DIGIT_TILE = 0x86, BLANK_TILE = 0x81, COMMA_TILE = 0x82;
  private static final int SCORE_COLUMN = 7, WINDOW_X = 7, WINDOW_TOP = 134, WINDOW_BOTTOM = 144;
  private static final int LOW_BALL_Y = 132, WINDOW_SPEED = 3;
  private static final int COLUMNS = 20;
  private final byte[] tiles = new byte[COLUMNS * 2];
  private final int[] drawnGroups = new int[COLUMNS / 4 * 2];
  private char drawnVersion;
  private byte drawnBalls, drawnParty;
  private boolean drawnCharge;
  private boolean valid;
  public byte windowY = (byte) WINDOW_BOTTOM;

  public static int nextWindowY(final int stage, final int y, final int previous) {
    if ((stage & 1) == 0) {
      return WINDOW_TOP;
    }
    if (y < LOW_BALL_Y) {
      final int next = previous - WINDOW_SPEED;
      return next < WINDOW_TOP ? WINDOW_TOP : next;
    }
    final int next = previous + WINDOW_SPEED;
    return next > WINDOW_BOTTOM ? WINDOW_BOTTOM : next;
  }

  public void invalidate(final int stage) {
    valid = false;
    windowY = (byte) ((stage & 1) == 0 ? WINDOW_TOP : WINDOW_BOTTOM);
  }

  @Mutable
  public void format(final PokemonScore score) {
    for (int i = 0; i < DIGITS; i++) {
      tiles[SCORE_COLUMN + i] = (byte) BLANK_TILE;
      tiles[COLUMNS + SCORE_COLUMN + i] = (byte) BLANK_TILE;
    }
    boolean leading = true;
    byte commaPhase = 0;
    int part = 0;
    for (int i = 0; i < DIGITS - 1; i++) {
      if ((i & 1) == 0) {
        part = score.byteAt((DIGITS - 2 - i) >>> 1);
      }
      final int digit = (i & 1) == 0 ? part >>> 4 : part & 15;
      if (digit != 0 || !leading || i == DIGITS - 2) {
        leading = false;
        tiles[SCORE_COLUMN + i] = (byte) (DIGIT_TILE + digit);
        if (commaPhase == 0) {
          tiles[COLUMNS + SCORE_COLUMN + i] = (byte) COMMA_TILE;
        }
      }
      commaPhase++;
      if (commaPhase == 3) { commaPhase = 0; }
    }
    tiles[COLUMNS - 1] = (byte) DIGIT_TILE;
  }

  public int tileAt(final int index) {
    return tiles[index < DIGITS ? SCORE_COLUMN + index : COLUMNS + SCORE_COLUMN + index - DIGITS] & BYTE_MASK;
  }

  @Mutable
  public void formatIcons(final int party, final int life, final boolean charged) {
    for (int i = 0; i < SCORE_COLUMN; i++) {
      tiles[i] = (byte) BLANK_TILE;
      tiles[COLUMNS + i] = (byte) BLANK_TILE;
    }
    tiles[0] = (byte) 0x83;
    int position = 1;
    if (party >= 100) {
      tiles[position++] = (byte) (DIGIT_TILE + party / 100);
    }
    if (party >= 10) {
      tiles[position++] = (byte) (DIGIT_TILE + (party / 10) % 10);
    }
    tiles[position] = (byte) (DIGIT_TILE + party % 10);
    tiles[4] = (byte) 0x85;
    tiles[5] = (byte) (DIGIT_TILE + (life ^ 3) + 1);
    tiles[6] = (byte) (charged ? 0x84 : BLANK_TILE);
  }

  public int iconAt(final int index) {
    return tiles[index] & BYTE_MASK;
  }

  @Mutable
  @NoInline
  public void draw(final PokemonBall ball, final PokemonRules rules, final int balls) {
    final PokemonScore score = rules.score;
    final int party = rules.partyCount & BYTE_MASK;
    final boolean charged = rules.spinner.charge == PokemonSpinner.MAX_CHARGE;
    final int nextY = nextWindowY(ball.stage, ball.y >>> FRACTION_BITS, windowY & BYTE_MASK);
    if (!valid || nextY != (windowY & BYTE_MASK)) {
      PokemonHardware.window(WINDOW_X, nextY);
    }
    windowY = (byte) nextY;
    if (valid && drawnVersion == score.version && drawnBalls == balls && (drawnParty & BYTE_MASK) == party && drawnCharge == charged) {
      return;
    }
    final boolean scoreChanged = !valid || drawnVersion != score.version;
    final boolean iconsChanged = !valid || drawnBalls != balls || (drawnParty & BYTE_MASK) != party || drawnCharge != charged;
    if (!scoreChanged && !iconsChanged) {
      return;
    }
    if (scoreChanged) { format(score); }
    if (iconsChanged) { formatIcons(party, 4 - balls, charged); }
    final int first = iconsChanged ? 0 : 4, end = scoreChanged ? COLUMNS : 8;
    for (int row = 0; row < 2; row++) {
      for (int x = first; x < end; x += 4) {
        final int offset = row * COLUMNS + x;
        final int packed = RomMemory.readIntLE(tiles,offset);
        final int group = row * (COLUMNS / 4) + (x >>> 2);
        if (!valid || drawnGroups[group] != packed) {
          PokemonHardware.windowPacked(x, row, packed);
          drawnGroups[group] = packed;
        }
      }
    }
    drawnVersion = score.version;
    drawnBalls = (byte) balls;
    drawnParty = (byte) party;
    drawnCharge = charged;
    valid = true;
  }
}

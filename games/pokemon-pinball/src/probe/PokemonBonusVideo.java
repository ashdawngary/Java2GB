package probe;

public final class PokemonBonusVideo {
  private PokemonBonusVideo() {}
  private static byte stage = -1, tens, ones, ends;

  public static void invalidate() { stage = -1; }

  public static void draw(final PokemonBall ball, final PokemonRules rules, final boolean color) {
    if ((ball.stage & 1) == 0) { return; }
    final PokemonBonusMultiplier bonus = rules.bonus;
    final boolean changed = stage != ball.stage;
    final int style = (ball.stage == PokemonConstants.BLUE_BOTTOM ? 2 : 0) | (color ? 1 : 0);
    if (changed || tens != bonus.tens) {
      PokemonBonusGraphics.paint((style << 6) | ((bonus.tens & 128) != 0 ? 16 : 0) | (bonus.tens & 127));
      tens = bonus.tens;
    }
    if (changed || ones != bonus.ones) {
      PokemonBonusGraphics.paint((style << 6) | 32 | ((bonus.ones & 128) != 0 ? 16 : 0) | (bonus.ones & 127));
      ones = bonus.ones;
    }
    if (style >= 2 && (changed || ends != bonus.endMask)) {
      final int base = 256 + (color ? 4 : 0);
      PokemonBonusGraphics.paint(base + (bonus.endMask & 1));
      PokemonBonusGraphics.paint(base + 2 + ((bonus.endMask >>> 1) & 1));
      ends = bonus.endMask;
    }
    stage = ball.stage;
  }
}

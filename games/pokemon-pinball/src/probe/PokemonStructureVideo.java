package probe;

import static probe.PokemonConstants.*;

public final class PokemonStructureVideo {
  private PokemonStructureVideo() {}
  private static byte painted = -1, requested = -1;
  public static void invalidate() {
    painted = -1;
    requested = -1;
  }
  public static void draw(final PokemonBall ball, final boolean color) {
    if (ball.stage != RED_TOP) {
      painted = -1;
      requested = -1;
      return;
    }
    final int previous = painted < 0 ? (color ? PokemonStructureData.INITIAL_CGB : PokemonStructureData.INITIAL_DMG) : painted;
    final int current = ball.collisionState & 7;
    if (requested != current) {
      painted = (byte) (previous != current && PokemonStructureData.paint(color,previous,current) ? current : previous);
      requested = (byte) current;
    }
  }
}

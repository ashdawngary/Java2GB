package probe;

import static probe.PokemonConstants.*;

public final class PokemonLightVideo {
  private PokemonLightVideo() {}
  private static byte drawnStage = -1, drawnCave = -1, drawnUpgrade = -1;
  private static boolean upgradeValid;

  public static void invalidate() {
    drawnStage = -1;
    upgradeValid = false;
  }

  public static void draw(final PokemonBall ball, final PokemonRules rules, final boolean color) {
    final int stage = ball.stage;
    final boolean changed = drawnStage != stage;
    final int style = (stage >= BLUE_TOP ? 2 : 0) | (color ? 1 : 0);
    if ((stage & 1) != 0) {
      if (changed || drawnCave != rules.caveMask) {
        PokemonLightData.cave(style, rules.caveMask);
        drawnCave = rules.caveMask;
      }
      upgradeValid = false;
    } else if ((ball.collisionState & 1) != 0) {
      if (changed || !upgradeValid || drawnUpgrade != rules.upgradeMask) {
        PokemonLightData.upgrade(style, rules.upgradeMask);
        drawnUpgrade = rules.upgradeMask;
        upgradeValid = true;
      }
    } else {
      upgradeValid = false;
    }
    drawnStage = (byte) stage;
  }
}

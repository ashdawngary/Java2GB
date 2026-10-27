package probe;
import dev.java2gb.api.*;

import static probe.PokemonConstants.*;

public final class PokemonPinballGame {
  private PokemonPinballGame() {}
  static final PokemonBall ball;
  static final PokemonRules rules;
  static final PokemonHud hud;
  static byte scene, previous, lives, cameraX = SCROLL_MAX;
  static boolean blue, color, paused;
  static char frames;

  static {
    GameBoy.init();
    PokemonHardware.init();
    color = PokemonHardware.color() != 0;
    ball = new PokemonBall();
    rules = new PokemonRules();
    hud = new PokemonHud();
  }

  public static void main(final String[] args) {
    PokemonGameFlow.load(TITLE_ASSET);
    while (true) {
      GameBoy.waitFrame();
      PokemonGameFlow.tick(GameBoy.buttons());
    }
  }
}

package probe;

public final class PokemonBonusGraphicsChecks {
  private PokemonBonusGraphicsChecks() {}
  static byte command, completed = -1;
  static char code;
  public static void main(final String[] args) {
    GameBoy.init();
    PokemonHardware.init();
    while (true) {
      final byte request = command;
      if (request != completed) {
        PokemonBonusGraphics.paint(code);
        completed = request;
      }
    }
  }
}

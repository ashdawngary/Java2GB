package probe;
import dev.java2gb.api.*;

public final class PokemonSceneChecks {
  private PokemonSceneChecks() {}
  static byte command, completed = -1;
  public static void main(final String[] args) {
    GameBoy.init();
    PokemonHardware.init();
    final boolean color = GameBoyHardware.color();
    while (true) {
      final byte request = command;
      if (completed != request) {
        GameBoyHardware.write(0xff4f,1);
        PokemonSceneData.load(request + (color ? PokemonConstants.SCENES_PER_MODEL : 0));
        completed = request;
      }
      GameBoy.waitFrame();
    }
  }
}

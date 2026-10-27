package probe;
import dev.java2gb.api.*;

public final class PokemonLightMotionChecks {
  private PokemonLightMotionChecks() {}
  public static void main(final String[] args) {
    GameBoy.report(PokemonLightChecks.motion());
  }
}

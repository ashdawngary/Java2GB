package probe;
import dev.java2gb.api.*;

public final class PokemonLightDecayChecks {
  private PokemonLightDecayChecks() {}
  public static void main(final String[] args) {
    GameBoy.report(PokemonLightChecks.decay());
  }
}

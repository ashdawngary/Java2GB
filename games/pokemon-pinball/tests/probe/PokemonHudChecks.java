package probe;
import dev.java2gb.api.*;

public final class PokemonHudChecks {
  private PokemonHudChecks() {}
  public static void main(final String[] args) {
    GameBoy.report(PokemonRuleChecks.hud());
  }
}

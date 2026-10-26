package probe;

public final class PokemonHudChecks {
  private PokemonHudChecks() {}
  public static void main(final String[] args) {
    GameBoy.report(PokemonRuleChecks.hud());
  }
}

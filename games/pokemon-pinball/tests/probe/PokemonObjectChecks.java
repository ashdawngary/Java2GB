package probe;
import dev.java2gb.api.*;

public final class PokemonObjectChecks {
  private PokemonObjectChecks() {}
  public static void main(final String[] args) {
    GameBoy.report(PokemonRuleChecks.objects());
  }
}

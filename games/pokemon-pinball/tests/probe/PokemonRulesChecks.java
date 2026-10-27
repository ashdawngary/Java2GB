package probe;
import dev.java2gb.api.*;

public final class PokemonRulesChecks {
  private PokemonRulesChecks() {}
  public static void main(final String[] args) {
    final int scores = PokemonRuleChecks.scores();
    final int queue = PokemonRuleChecks.queue();
    GameBoy.report(scores < 0 ? scores : queue < 0 ? queue : scores + queue);
  }
}

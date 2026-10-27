package probe;
import dev.java2gb.api.*;

public final class PokemonContactChecks {
  private PokemonContactChecks() {}
  @NoInline
  public static int run() {
    for (int points = 1; points < PokemonConstants.WORD_MASK; points++) {
      if (PokemonPhysics.contactKey(points) != PokemonContactCases.key(points)) { return -points; }
    }
    return PokemonConstants.WORD_MASK - 1;
  }
  public static void main(final String[] args) { GameBoy.report(run()); }
}

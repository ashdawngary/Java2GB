package probe;
import dev.java2gb.api.*;

public final class PokemonChecks {
  private PokemonChecks() {}
  public static void main(final String[] args) {
    GameBoy.report(PokemonArithmetic.run());
  }
}

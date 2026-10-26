package probe;

public final class PokemonMultiplierOriginalChecks {
  private PokemonMultiplierOriginalChecks() {}
  public static void main(final String[] args) {
    final int result = PokemonMultiplierChecks.run();
    if (result < 0) { throw new AssertionError("Bonus multiplier case " + (-result - 1)); }
    System.out.println(result);
  }
}

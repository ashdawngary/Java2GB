package probe;
public final class PokemonPikachuOriginalChecks {
  private PokemonPikachuOriginalChecks() {}
  public static void main(final String[] args) {
    final int result = PokemonPikachuComparisons.run();
    if (result < 0) { throw new AssertionError("Pikachu case " + (-result - 1)); }
    System.out.println(result);
  }
}

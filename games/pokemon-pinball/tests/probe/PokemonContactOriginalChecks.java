package probe;

public final class PokemonContactOriginalChecks {
  private PokemonContactOriginalChecks() {}
  public static void main(final String[] args) {
    final int result = PokemonContactChecks.run();
    if (result < 0) { throw new AssertionError("Contact mask " + Integer.toHexString(-result)); }
    System.out.println(result);
  }
}

package probe;

public final class PokemonArithmetic {
  private PokemonArithmetic() {}
  public static int run() {
    int hash = 1;
    for (int angle = 0; angle < 256; angle++) {
      for (int i = 0; i < 8; i++) {
        final short x = (short) (i * 5371 - 17000), y = (short) (i * 3197 - 9200);
        hash = hash * 31 + PokemonPhysics.rotate(x, y, angle);
        hash = hash * 31 + PokemonPhysics.limit(x);
        hash = hash * 31 + PokemonPhysics.displacement(y);
      }
    }
    int state = 0;
    for (int i = 0; i < 800; i++) {
      final int change = PokemonPhysics.flipperChange(state, (i % 19) < 11);
      state = PokemonPhysics.flipperState(state, change);
      hash = hash * 31 + state;
    }
    return hash;
  }
}

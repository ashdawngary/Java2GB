package probe;

import java.io.DataInputStream;
import java.io.FileInputStream;

public final class PokemonOriginalChecks {
  private PokemonOriginalChecks() {}
  public static void main(final String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println(PokemonArithmetic.run());
      return;
    }
    int compared = 0;
    try (final DataInputStream input = new DataInputStream(new FileInputStream(args[0]))) {
      final int count = input.readInt();
      for (int i = 0; i < count; i++) {
        final int kind = input.readInt(), a = input.readInt(), b = input.readInt();
        final int c = input.readInt(), expected = input.readInt();
        final int actual;
        if (kind == 0) {
          actual = PokemonPhysics.rotate(a, b, c);
        } else if (kind == 1) {
          actual = PokemonPhysics.limit(a);
        } else if (kind == 2) {
          actual = PokemonPhysics.displacement(a);
        } else {
          final int change = PokemonPhysics.flipperChange(a, b != 0);
          actual = PokemonPhysics.flipperState(a, change) | (change << 16);
        }
        if (actual != expected) {
          throw new AssertionError("Original case " + i + " kind=" + kind + " input=" + a + "," + b + "," + c
              + " expected=" + expected + " actual=" + actual);
        }
        compared++;
      }
    }
    System.out.println(compared);
  }
}

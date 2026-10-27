package probe;
import dev.java2gb.api.*;

public final class PokemonMultiplierChecks {
  private PokemonMultiplierChecks() {}
  public static int run() {
    final PokemonBonusMultiplier counter = new PokemonBonusMultiplier();
    for (int i = 0; i < PokemonMultiplierCases.COUNT; i++) {
      counter.value = (byte) PokemonMultiplierCases.c3(i);
      counter.left = (byte) PokemonMultiplierCases.c4(i);
      counter.right = (byte) PokemonMultiplierCases.c5(i);
      counter.delay = (byte) PokemonMultiplierCases.c6(i);
      counter.tens = (byte) PokemonMultiplierCases.c7(i);
      counter.ones = (byte) PokemonMultiplierCases.c8(i);
      counter.endLight = (byte) PokemonMultiplierCases.c9(i);
      counter.messageTens = (byte) PokemonMultiplierCases.c10(i);
      counter.messageOnes = (byte) PokemonMultiplierCases.c11(i);
      counter.message = false;
      counter.tick(PokemonMultiplierCases.c0(i) != 0,(short) PokemonMultiplierCases.c1(i),PokemonMultiplierCases.c2(i));
      if ((counter.value & 255) != PokemonMultiplierCases.c14(i)
          || (counter.left & 255) != PokemonMultiplierCases.c15(i) || (counter.right & 255) != PokemonMultiplierCases.c16(i)
          || (counter.delay & 255) != PokemonMultiplierCases.c17(i) || (counter.tens & 255) != PokemonMultiplierCases.c18(i)
          || (counter.ones & 255) != PokemonMultiplierCases.c19(i) || (counter.endLight & 255) != PokemonMultiplierCases.c20(i)
          || (counter.messageTens & 255) != PokemonMultiplierCases.c21(i) || (counter.messageOnes & 255) != PokemonMultiplierCases.c22(i)
          || (counter.message ? 1 : 0) != PokemonMultiplierCases.c23(i)
          || (counter.extraBall ? 1 : 0) != PokemonMultiplierCases.c24(i)) {
        return -i - 1;
      }
    }
    return PokemonMultiplierCases.COUNT;
  }
  public static void main(final String[] args) { GameBoy.report(run()); }
}

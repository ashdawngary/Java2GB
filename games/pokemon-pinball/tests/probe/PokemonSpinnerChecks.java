package probe;

public final class PokemonSpinnerChecks {
  private PokemonSpinnerChecks() {}

  public static int run() {
    final PokemonSpinner spinner = new PokemonSpinner();
    for (int i = 0; i < PokemonSpinnerCases.COUNT; i++) {
      spinner.velocity = (short) PokemonSpinnerCases.c0(i);
      spinner.state = (char) PokemonSpinnerCases.c1(i);
      spinner.charge = (byte) PokemonSpinnerCases.c2(i);
      spinner.turns = (byte) PokemonSpinnerCases.c3(i);
      spinner.chargeLock = (byte) PokemonSpinnerCases.c4(i);
      final boolean turn = spinner.tick();
      if ((spinner.velocity & 65535) != PokemonSpinnerCases.c5(i) || spinner.state != PokemonSpinnerCases.c6(i)
          || (spinner.charge & 255) != PokemonSpinnerCases.c7(i) || (spinner.turns & 255) != PokemonSpinnerCases.c8(i)
          || (spinner.chargeLock & 255) != PokemonSpinnerCases.c9(i) || turn != (PokemonSpinnerCases.c10(i) != 0)) {
        return -i - 1;
      }
    }
    return PokemonSpinnerCases.COUNT;
  }

  public static void main(final String[] args) {
    GameBoy.report(run());
  }
}

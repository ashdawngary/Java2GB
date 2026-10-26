package probe;

public final class PokemonIconChecks {
  private PokemonIconChecks() {}

  public static int run() {
    final PokemonHud hud = new PokemonHud();
    for (int i = 0; i < PokemonIconCases.COUNT; i++) {
      hud.formatIcons(PokemonIconCases.c0(i), PokemonIconCases.c1(i), PokemonIconCases.c2(i) == PokemonSpinner.MAX_CHARGE);
      if (hud.iconAt(0) != PokemonIconCases.c3(i) || hud.iconAt(1) != PokemonIconCases.c4(i)
          || hud.iconAt(2) != PokemonIconCases.c5(i) || hud.iconAt(3) != PokemonIconCases.c6(i)
          || hud.iconAt(4) != PokemonIconCases.c7(i) || hud.iconAt(5) != PokemonIconCases.c8(i)
          || hud.iconAt(6) != PokemonIconCases.c9(i)) {
        return -i - 1;
      }
    }
    return PokemonIconCases.COUNT;
  }

  public static void main(final String[] args) {
    GameBoy.report(run());
  }
}

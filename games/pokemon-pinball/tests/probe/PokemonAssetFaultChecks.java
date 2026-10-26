package probe;

public final class PokemonAssetFaultChecks {
  private PokemonAssetFaultChecks() {}
  public static void main(final String[] args) {
    GameBoy.init();
    while (true) {
      final int buttons = GameBoy.buttons();
      if (buttons != 0) {
        final int asset = buttons == GameBoy.RIGHT ? -1 : buttons == GameBoy.A ? 23 : 0;
        final int offset = buttons == GameBoy.LEFT ? -4 : buttons == GameBoy.UP ? 16381
            : buttons == GameBoy.DOWN ? 0x7fffffff : 0;
        GameBoy.report(PokemonRomData.data4(asset, offset));
      }
    }
  }
}

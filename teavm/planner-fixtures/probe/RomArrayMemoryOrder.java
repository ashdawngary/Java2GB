package probe;

public final class RomArrayMemoryOrder {
  private RomArrayMemoryOrder() {}
  @RomData(bank=2) static final byte[] FIRST = {1,2,3,4};
  @RomData(bank=3) static final byte[] SECOND = {5,6,7,8};
  static byte[] selected = FIRST;
  static int scenario, effects;

  @NoInline static int offset() {
    effects++;
    selected = SECOND;
    return 0;
  }

  public static void main(final String[] args) {
    while (GameBoy.buttons() == 0) { }
    if (scenario != 0) {
      selected = null;
    }
    final int value = RomMemory.readIntLE(selected, offset());
    GameBoy.report(value == 0x04030201 && effects == 1 && selected == SECOND ? 1 : -1);
  }
}

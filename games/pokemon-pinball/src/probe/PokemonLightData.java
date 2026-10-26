package probe;

public final class PokemonLightData {
  private PokemonLightData() {}
  @NoInline public static void cave(final int style, final int mask) {
    switch (style) {
      case 0:
        GameBoy.background(1,9, (mask & 1) != 0 ? 125 : 124);
        GameBoy.background(3,9, (mask & 2) != 0 ? 125 : 124);
        GameBoy.background(16,9, (mask & 4) != 0 ? 127 : 126);
        GameBoy.background(18,9, (mask & 8) != 0 ? 127 : 126);
        return;
      case 1:
        GameBoy.background(1,9, (mask & 1) != 0 ? 39 : 38);
        GameBoy.background(3,9, (mask & 2) != 0 ? 41 : 40);
        GameBoy.background(16,9, (mask & 4) != 0 ? 126 : 124);
        GameBoy.background(18,9, (mask & 8) != 0 ? 127 : 125);
        return;
      case 2:
        GameBoy.background(1,9, (mask & 1) != 0 ? 94 : 93);
        GameBoy.background(3,9, (mask & 2) != 0 ? 94 : 93);
        GameBoy.background(16,9, (mask & 4) != 0 ? 96 : 95);
        GameBoy.background(18,9, (mask & 8) != 0 ? 96 : 95);
        return;
      case 3:
        GameBoy.background(1,9, (mask & 1) != 0 ? 73 : 71);
        GameBoy.background(3,9, (mask & 2) != 0 ? 74 : 72);
        GameBoy.background(16,9, (mask & 4) != 0 ? 75 : 122);
        GameBoy.background(18,9, (mask & 8) != 0 ? 76 : 123);
        return;
      default: return;
    }
  }
  @NoInline public static void upgrade(final int style, final int mask) {
    switch (style) {
      case 0:
        GameBoy.background(7,7, (mask & 1) != 0 ? 172 : 102);
        GameBoy.background(9,6, (mask & 2) != 0 ? 173 : 104);
        GameBoy.background(10,6, (mask & 2) != 0 ? 174 : 105);
        GameBoy.background(12,6, (mask & 4) != 0 ? 175 : 106);
        return;
      case 1:
        GameBoy.background(7,7, (mask & 1) != 0 ? 61 : 55);
        GameBoy.background(9,6, (mask & 2) != 0 ? 63 : 57);
        GameBoy.background(10,6, (mask & 2) != 0 ? 64 : 58);
        GameBoy.background(12,6, (mask & 4) != 0 ? 65 : 59);
        return;
      case 2:
        GameBoy.background(6,4, (mask & 1) != 0 ? 102 : 100);
        GameBoy.background(7,4, (mask & 1) != 0 ? 103 : 101);
        GameBoy.background(9,3, (mask & 2) != 0 ? 102 : 100);
        GameBoy.background(10,3, (mask & 2) != 0 ? 103 : 101);
        GameBoy.background(12,4, (mask & 4) != 0 ? 102 : 100);
        GameBoy.background(13,4, (mask & 4) != 0 ? 103 : 101);
        return;
      case 3:
        GameBoy.background(6,4, (mask & 1) != 0 ? 67 : 66);
        GameBoy.background(7,4, (mask & 1) != 0 ? 67 : 66);
        GameBoy.background(9,3, (mask & 2) != 0 ? 67 : 66);
        GameBoy.background(10,3, (mask & 2) != 0 ? 67 : 66);
        GameBoy.background(12,4, (mask & 4) != 0 ? 67 : 66);
        GameBoy.background(13,4, (mask & 4) != 0 ? 67 : 66);
        return;
      default: return;
    }
  }
}

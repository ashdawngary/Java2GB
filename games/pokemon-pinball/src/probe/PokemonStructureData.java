package probe;
import dev.java2gb.api.*;

public final class PokemonStructureData {
  private PokemonStructureData() {}
  public static final int INITIAL_DMG = 0, INITIAL_CGB = 0;
  @NoInline public static boolean paint(final boolean color, final int previous, final int current) {
    switch ((color ? 64 : 0) | (previous << 3) | current) {
      case 1:
      case 19:
      case 37:
      case 55:
        PokemonVideoData.copy(0);
        return true;
      case 2:
      case 11:
        PokemonVideoData.copy(1);
        return true;
      case 3:
        PokemonVideoData.copy(2);
        return true;
      case 4:
      case 13:
        PokemonVideoData.copy(3);
        return true;
      case 5:
        PokemonVideoData.copy(4);
        return true;
      case 6:
      case 15:
      case 38:
      case 47:
        PokemonVideoData.copy(1);
        PokemonVideoData.copy(5);
        return true;
      case 7:
        PokemonVideoData.copy(6);
        return true;
      case 20:
      case 29:
      case 52:
      case 61:
        PokemonVideoData.copy(7);
        PokemonVideoData.copy(3);
        return true;
      case 22:
      case 31:
        PokemonVideoData.copy(5);
        return true;
      case 26:
      case 44:
      case 62:
        PokemonVideoData.copy(8);
        return true;
      case 34:
      case 43:
        PokemonVideoData.copy(1);
        PokemonVideoData.copy(9);
        return true;
      case 50:
      case 59:
        PokemonVideoData.copy(9);
        return true;
      case 65:
      case 83:
      case 101:
      case 119:
        PokemonVideoData.copy(10);
        PokemonVideoData.copy(11);
        PokemonVideoData.copy(12);
        PokemonVideoData.copy(13);
        PokemonVideoData.copy(14);
        PokemonVideoData.copy(15);
        return true;
      case 66:
      case 75:
        PokemonVideoData.copy(16);
        PokemonVideoData.copy(17);
        PokemonVideoData.copy(18);
        PokemonVideoData.copy(19);
        PokemonVideoData.copy(20);
        PokemonVideoData.copy(21);
        PokemonVideoData.copy(22);
        PokemonVideoData.copy(23);
        return true;
      case 67:
        PokemonVideoData.copy(16);
        PokemonVideoData.copy(17);
        PokemonVideoData.copy(18);
        PokemonVideoData.copy(24);
        PokemonVideoData.copy(11);
        PokemonVideoData.copy(20);
        PokemonVideoData.copy(12);
        PokemonVideoData.copy(21);
        PokemonVideoData.copy(13);
        PokemonVideoData.copy(14);
        PokemonVideoData.copy(22);
        PokemonVideoData.copy(15);
        PokemonVideoData.copy(23);
        return true;
      case 68:
      case 77:
        PokemonVideoData.copy(25);
        PokemonVideoData.copy(26);
        PokemonVideoData.copy(27);
        PokemonVideoData.copy(28);
        return true;
      case 69:
        PokemonVideoData.copy(10);
        PokemonVideoData.copy(11);
        PokemonVideoData.copy(12);
        PokemonVideoData.copy(25);
        PokemonVideoData.copy(13);
        PokemonVideoData.copy(14);
        PokemonVideoData.copy(26);
        PokemonVideoData.copy(15);
        PokemonVideoData.copy(27);
        PokemonVideoData.copy(28);
        return true;
      case 70:
      case 79:
      case 102:
      case 111:
        PokemonVideoData.copy(16);
        PokemonVideoData.copy(17);
        PokemonVideoData.copy(18);
        PokemonVideoData.copy(19);
        PokemonVideoData.copy(20);
        PokemonVideoData.copy(21);
        PokemonVideoData.copy(29);
        PokemonVideoData.copy(22);
        PokemonVideoData.copy(30);
        PokemonVideoData.copy(23);
        PokemonVideoData.copy(31);
        PokemonVideoData.copy(32);
        return true;
      case 71:
        PokemonVideoData.copy(16);
        PokemonVideoData.copy(17);
        PokemonVideoData.copy(18);
        PokemonVideoData.copy(24);
        PokemonVideoData.copy(11);
        PokemonVideoData.copy(20);
        PokemonVideoData.copy(12);
        PokemonVideoData.copy(21);
        PokemonVideoData.copy(29);
        PokemonVideoData.copy(13);
        PokemonVideoData.copy(14);
        PokemonVideoData.copy(22);
        PokemonVideoData.copy(30);
        PokemonVideoData.copy(15);
        PokemonVideoData.copy(23);
        PokemonVideoData.copy(31);
        PokemonVideoData.copy(32);
        return true;
      case 84:
      case 93:
      case 116:
      case 125:
        PokemonVideoData.copy(33);
        PokemonVideoData.copy(34);
        PokemonVideoData.copy(35);
        PokemonVideoData.copy(36);
        PokemonVideoData.copy(37);
        PokemonVideoData.copy(38);
        PokemonVideoData.copy(25);
        PokemonVideoData.copy(39);
        PokemonVideoData.copy(26);
        PokemonVideoData.copy(40);
        PokemonVideoData.copy(27);
        PokemonVideoData.copy(28);
        return true;
      case 86:
      case 95:
        PokemonVideoData.copy(29);
        PokemonVideoData.copy(30);
        PokemonVideoData.copy(31);
        PokemonVideoData.copy(32);
        return true;
      case 90:
      case 108:
      case 126:
        PokemonVideoData.copy(41);
        PokemonVideoData.copy(42);
        PokemonVideoData.copy(43);
        PokemonVideoData.copy(44);
        PokemonVideoData.copy(45);
        PokemonVideoData.copy(46);
        return true;
      case 98:
      case 107:
        PokemonVideoData.copy(16);
        PokemonVideoData.copy(17);
        PokemonVideoData.copy(18);
        PokemonVideoData.copy(19);
        PokemonVideoData.copy(20);
        PokemonVideoData.copy(21);
        PokemonVideoData.copy(47);
        PokemonVideoData.copy(22);
        PokemonVideoData.copy(30);
        PokemonVideoData.copy(23);
        PokemonVideoData.copy(31);
        PokemonVideoData.copy(32);
        return true;
      case 114:
      case 123:
        PokemonVideoData.copy(47);
        PokemonVideoData.copy(30);
        PokemonVideoData.copy(31);
        PokemonVideoData.copy(32);
        return true;
      default: return false;
    }
  }
}

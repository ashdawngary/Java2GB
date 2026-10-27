package probe;
import dev.java2gb.api.*;

public final class PokemonData {
  private PokemonData() {}
  @RomData(bank = 0) private static final byte[] SINE = {
    0,6,13,19,25,31,38,44,50,56,62,68,74,80,86,92,98,104,109,115,121,126,-124,-119,-114,-109,-104,-99,-94,-89,-84,-79,
    -75,-71,-66,-62,-58,-54,-50,-47,-43,-40,-36,-33,-30,-27,-25,-22,-19,-17,-15,-13,-11,-9,-8,-6,-5,-4,-3,-2,-1,-1,-1,-1,
    -1,
  };
  @RomData(bank = 0) private static final byte[] POINTS = {
    4,0,4,1,3,3,1,4,0,4,-1,4,-3,3,-4,1,-4,0,-4,-1,-3,-3,-1,-4,0,-4,1,-4,3,-3,4,-1,
  };
  @RomData(bank = 0) private static final byte[] NORMALS = {
    -64,-59,-48,-37,-32,-27,-16,-5,0,5,16,27,32,37,48,59,69,-54,-43,-32,-27,-22,-11,0,5,10,21,32,37,42,53,64,
    80,85,-32,-21,-16,-11,0,11,16,21,32,43,48,53,64,75,91,96,107,-10,-5,0,11,22,27,32,43,54,59,64,75,86,
    96,101,112,123,0,5,16,27,32,37,48,59,64,69,80,91,101,106,117,-128,-123,10,21,32,37,42,53,64,69,74,85,96,
    112,117,-128,-117,-112,-107,32,43,48,53,64,75,80,85,96,107,123,-128,-117,-106,-101,-96,-85,54,59,64,75,86,91,96,107,118,
    -128,-123,-112,-101,-96,-91,-80,-69,64,69,80,91,96,101,112,123,-123,-118,-107,-96,-91,-86,-75,-64,-59,74,85,96,101,106,117,-128,
    -112,-107,-96,-85,-80,-75,-64,-53,-48,-43,96,107,112,117,-128,-117,-101,-96,-85,-74,-69,-64,-53,-42,-37,-32,-21,118,123,-128,-117,-106,
    -96,-91,-80,-69,-64,-59,-48,-37,-32,-27,-16,-5,-128,-123,-112,-101,-91,-86,-75,-64,-59,-54,-43,-32,-27,-22,-11,0,5,-118,-107,-96,
    -80,-75,-64,-53,-48,-43,-32,-21,-16,-11,0,11,16,21,-96,-85,-69,-64,-53,-42,-37,-32,-21,-10,-5,0,11,22,27,32,43,-74,
  };
  @RomData(bank = 0) private static final short[] DX = {
    -256,-261,-308,-372,-393,-399,-339,-141,0,172,639,1175,1417,1645,2056,2279,2279,-248,-255,-284,-290,-285,-206,0,141,311,765,1277,1505,1718,2092,2273,
    2056,1972,-181,-144,-127,-103,0,206,339,496,905,1351,1544,1720,2004,2092,1645,1526,1117,-62,-32,0,103,285,399,532,876,1241,1394,1528,1720,1718,
    1417,1284,851,281,0,32,127,290,393,512,820,1145,1279,1394,1544,1505,1175,1030,579,0,-281,62,144,284,372,476,747,1031,1145,1241,1351,1277,
    639,476,0,-579,-851,-1117,181,255,308,374,555,747,820,876,905,765,172,0,-476,-1030,-1284,-1526,-1972,248,261,286,374,476,512,532,496,311,
    0,-172,-639,-1175,-1417,-1645,-2056,-2279,256,261,308,372,393,399,339,141,-141,-311,-765,-1277,-1505,-1718,-2092,-2273,-2279,248,255,284,290,285,206,0,
    -339,-496,-905,-1351,-1544,-1720,-2004,-2092,-2056,-1972,181,144,127,103,0,-206,-399,-532,-876,-1241,-1394,-1528,-1720,-1718,-1645,-1526,-1117,62,32,0,-103,-285,
    -393,-512,-820,-1145,-1279,-1394,-1544,-1505,-1417,-1284,-851,-281,0,-32,-127,-290,-372,-476,-747,-1031,-1145,-1241,-1351,-1277,-1175,-1030,-579,0,281,-62,-144,-284,
    -308,-374,-555,-747,-820,-876,-905,-765,-639,-476,0,579,851,1117,-181,-255,-261,-286,-374,-476,-512,-532,-496,-311,-172,0,476,1030,1284,1526,1972,-248,
  };
  @RomData(bank = 0) private static final short[] DY = {
    0,-32,-127,-290,-393,-512,-820,-1145,-1279,-1394,-1544,-1505,-1417,-1284,-851,-281,281,-62,-144,-284,-372,-476,-747,-1031,-1145,-1241,-1351,-1277,-1175,-1030,-579,0,
    851,1117,-181,-255,-308,-374,-555,-747,-820,-876,-905,-765,-639,-476,0,579,1284,1526,1972,-248,-261,-286,-374,-476,-512,-532,-496,-311,-172,0,476,1030,
    1417,1645,2056,2279,-256,-261,-308,-372,-393,-399,-339,-141,0,172,639,1175,1505,1718,2092,2273,2279,-248,-255,-284,-290,-285,-206,0,141,311,765,1277,
    1544,1720,2004,2092,2056,1972,-181,-144,-127,-103,0,206,339,496,905,1351,1394,1528,1720,1718,1645,1526,1117,-62,-32,0,103,285,399,532,876,1241,
    1279,1394,1544,1505,1417,1284,851,281,0,32,127,290,393,512,820,1145,1145,1241,1351,1277,1175,1030,579,0,-281,62,144,284,372,476,747,1031,
    820,876,905,765,639,476,0,-579,-851,-1117,181,255,308,374,555,747,512,532,496,311,172,0,-476,-1030,-1284,-1526,-1972,248,261,286,374,476,
    393,399,339,141,0,-172,-639,-1175,-1417,-1645,-2056,-2279,256,261,308,372,290,285,206,0,-141,-311,-765,-1277,-1505,-1718,-2092,-2273,-2279,248,255,284,
    127,103,0,-206,-339,-496,-905,-1351,-1544,-1720,-2004,-2092,-2056,-1972,181,144,32,0,-103,-285,-399,-532,-876,-1241,-1394,-1528,-1720,-1718,-1645,-1526,-1117,62,
  };
  @RomData(bank = 0) private static final char[] RADIUS = {
    0,12,28,48,56,72,92,108,112,128,148,164,180,196,212,228,248,252,252,252,252,252,252,252,252,252,252,252,252,252,252,252,
  };
  @RomData(bank = 0) private static final byte[] LEFT_MASKS = {
    0,0,0,0,0,0,0,-128,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    -32,-16,-4,-2,-1,-1,-1,-1,0,0,0,0,0,-64,-32,-16,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    -1,-1,-1,-1,127,31,15,3,-4,-2,-1,-1,-1,-1,-1,-1,0,0,0,-64,-32,-16,-4,-2,0,0,0,0,0,0,0,0,
    1,0,0,0,0,0,0,0,-1,63,15,1,0,0,0,0,-1,-1,-1,-1,127,7,0,0,0,-128,-128,-128,-128,0,0,0,
    0,0,0,0,0,0,0,-128,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    -32,-16,-4,-2,-1,-1,-1,-1,0,0,0,0,0,-32,-1,-1,0,0,0,0,0,0,-64,-1,0,0,0,0,0,0,0,-128,
    -1,-1,-1,-1,127,31,15,3,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-8,0,-32,-16,-16,-16,-16,-64,0,0,
    1,0,0,0,0,0,0,0,-32,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,-128,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,-128,
    -32,-16,-4,-2,-1,-1,-1,-1,0,0,7,63,-1,-1,-1,-1,31,-1,-1,-1,-1,-1,-2,-4,-64,-64,-64,-64,-128,0,0,0,
    -1,-1,-1,-1,127,31,15,3,-1,-1,-1,-1,-2,-8,-16,-32,-16,-32,-128,0,0,0,0,0,0,0,0,0,0,0,0,0,
    1,0,0,0,0,0,0,0,-128,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
  };
  @RomData(bank = 0) private static final byte[] RIGHT_MASKS = {
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,7,15,7,15,63,127,-1,-1,-1,-1,
    0,0,0,0,0,0,0,0,0,0,0,3,7,15,63,127,63,127,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-2,-8,-16,-64,
    0,1,1,1,1,0,0,0,-1,-1,-1,-1,-2,-32,0,0,-1,-4,-16,-128,0,0,0,0,-128,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,
    0,0,0,0,0,0,0,1,0,0,0,0,0,0,3,-1,0,0,0,0,0,7,-1,-1,7,15,63,127,-1,-1,-1,-1,
    7,15,15,15,15,3,0,0,-1,-1,-1,-1,-1,-1,31,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-2,-8,-16,-64,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,0,0,0,0,0,0,0,-128,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,-64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,
    3,3,3,3,1,0,0,0,-8,-1,-1,-1,-1,-1,127,63,0,0,-32,-4,-1,-1,-1,-1,7,15,63,127,-1,-1,-1,-1,
    0,0,0,0,0,0,0,0,15,7,1,0,0,0,0,0,-1,-1,-1,-1,127,31,15,7,-1,-1,-1,-1,-2,-8,-16,-64,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,-128,0,0,0,0,0,0,0,
  };
  private static final int FLIPPER_RADII_A = 0;
  private static final int FLIPPER_RADII_B = 1;
  private static final int FLIPPER_ANGLES_A = 2;
  private static final int FLIPPER_ANGLES_B = 3;

  public static int sineMagnitude(final int index) { return SINE[index] & 255; }
  public static int pointX(final int index) { return POINTS[index * 2]; }
  public static int pointY(final int index) { return POINTS[index * 2 + 1]; }
  public static int normal(final int index) { return NORMALS[index] & 255; }
  public static int deltaX(final int index) { return DX[index]; }
  public static int deltaY(final int index) { return DY[index]; }
  public static int radiusMagnitude(final int index) { return RADIUS[index]; }
  public static int flipperRadius(final int index) {
    return index < 16384 ? PokemonRomData.read0( index) : PokemonRomData.read1( index - 16384);
  }
  public static int flipperAngle(final int index) {
    return index < 8192 ? PokemonRomData.read2( index) : PokemonRomData.read3( index - 8192);
  }
  public static int lowerMask(final boolean right, final int index) {
    return (right ? RIGHT_MASKS[index] : LEFT_MASKS[index]) & 255;
  }
  public static int lowerMask4(final boolean right, final int index) {
    return lowerMask(right,index) | (lowerMask(right,index+1)<<8)
        | (lowerMask(right,index+2)<<16) | (lowerMask(right,index+3)<<24);
  }
  private static final int RED_TOP_MAP_0 = 4;
  private static final int RED_TOP_MAP_1 = 5;
  private static final int RED_TOP_MAP_2 = 6;
  private static final int RED_TOP_MAP_3 = 7;
  private static final int RED_TOP_MAP_4 = 8;
  private static final int RED_TOP_MAP_5 = 9;
  private static final int RED_TOP_MAP_6 = 10;
  private static final int RED_TOP_MAP_7 = 11;
  private static final int RED_TOP_MASK_0 = 12;
  private static final int RED_TOP_MASK_1 = 13;
  private static final int RED_TOP_MASK_2 = 14;
  private static final int RED_TOP_MASK_3 = 15;
  private static final int RED_BOTTOM_MAP = 16;
  private static final int RED_BOTTOM_MASK = 17;
  private static final int BLUE_TOP_MAP_0 = 18;
  private static final int BLUE_TOP_MAP_1 = 19;
  private static final int BLUE_TOP_MASK = 20;
  private static final int BLUE_BOTTOM_MAP = 21;
  private static final int BLUE_BOTTOM_MASK = 22;

  @NoInline public static int attribute(final int stage, final int state, final int index) {
    if (index < 0 || index >= 768) { return 0; }
    if (stage == 1) { return PokemonRomData.read16( index); }
    if (stage == 5) { return PokemonRomData.read21( index); }
    if (stage == 4) { return (state == 0 ? PokemonRomData.read18( index) : PokemonRomData.read19( index)); }
    if (state == 0) { return PokemonRomData.read4( index); }
    if (state == 1) { return PokemonRomData.read5( index); }
    if (state == 2) { return PokemonRomData.read6( index); }
    if (state == 3) { return PokemonRomData.read7( index); }
    if (state == 4) { return PokemonRomData.read8( index); }
    if (state == 5) { return PokemonRomData.read9( index); }
    if (state == 6) { return PokemonRomData.read10( index); }
    return PokemonRomData.read11( index);
  }

  @NoInline public static int mask(final int stage, final int state, final int index) {
    if (stage == 1) { return PokemonRomData.read17( index); }
    if (stage == 5) { return PokemonRomData.read22( index); }
    if (stage == 4) { return PokemonRomData.read20( index); }
    if (state < 2) { return PokemonRomData.read12( index); }
    if (state < 4) { return PokemonRomData.read13( index); }
    if (state < 6) { return PokemonRomData.read14( index); }
    return PokemonRomData.read15( index);
  }
  @NoInline public static int mask4(final int stage, final int state, final int index) {
    if (stage == 1) { return PokemonRomData.read4_17( index); }
    if (stage == 5) { return PokemonRomData.read4_22( index); }
    if (stage == 4) { return PokemonRomData.read4_20( index); }
    if (state < 2) { return PokemonRomData.read4_12( index); }
    if (state < 4) { return PokemonRomData.read4_13( index); }
    if (state < 6) { return PokemonRomData.read4_14( index); }
    return PokemonRomData.read4_15( index);
  }
  @RomData(bank = 0) private static final byte[] SPRITES = {
    8,8,66,0,8,0,64,0,-128,8,8,70,0,8,0,68,0,-128,8,8,74,0,8,0,72,0,-128,8,8,78,0,8,
    0,76,0,-128,8,8,82,0,8,0,80,0,-128,8,8,86,0,8,0,84,0,-128,8,8,90,0,8,0,88,0,-128,8,
    8,94,0,8,0,92,0,-128,12,5,100,2,10,-3,98,2,20,-11,96,2,-128,12,5,106,2,4,-3,104,2,12,-11,102,
    2,-128,10,5,112,2,3,-3,110,2,-3,-11,108,2,-128,12,3,100,34,10,11,98,34,20,19,96,34,-128,12,3,106,34,
    4,11,104,34,12,19,102,34,-128,10,3,112,34,3,11,110,34,-3,19,108,34,-128,
  };
  @RomData(bank = 0) private static final byte[] SPRITE_OFFSETS = {
    0,9,18,27,36,45,54,63,72,85,98,111,124,-119,
  };

  public static int spriteOffset(final int frame) { return SPRITE_OFFSETS[frame] & 255; }
  public static int spriteByte(final int index) { return SPRITES[index]; }
}

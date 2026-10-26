package probe;

public final class PokemonObjectData {
  private PokemonObjectData() {}
  public static final int RED_VOLTORB = 0;
  public static final int RED_SPINNER = 1;
  public static final int RED_BOARD = 2;
  public static final int RED_STARYU_TOP = 3;
  public static final int RED_BELLSPROUT = 4;
  public static final int RED_DITTO = 5;
  public static final int RED_UPGRADE = 6;
  public static final int RED_WILD = 7;
  public static final int RED_STARYU_BOTTOM = 8;
  public static final int RED_DIGLETT = 9;
  public static final int RED_MULTIPLIER = 10;
  public static final int RED_SLOT = 11;
  public static final int RED_BUMPERS = 12;
  public static final int RED_PIKACHU = 13;
  public static final int RED_CAVE = 14;
  public static final int RED_LAUNCH = 15;
  public static final int BLUE_SHELLDER = 16;
  public static final int BLUE_SPINNER = 17;
  public static final int BLUE_BOARD = 18;
  public static final int BLUE_SLOWPOKE = 19;
  public static final int BLUE_CLOYSTER = 20;
  public static final int BLUE_UPGRADE = 21;
  public static final int BLUE_WILD = 22;
  public static final int BLUE_PSYDUCK = 23;
  public static final int BLUE_MULTIPLIER = 24;
  public static final int BLUE_SLOT = 25;
  public static final int BLUE_BUMPERS = 26;
  public static final int BLUE_PIKACHU = 27;
  public static final int BLUE_CAVE = 28;
  public static final int BLUE_LAUNCH = 29;
  @NoInline public static int first(final PokemonBall ball) {
    final int x = ball.x >>> 8, y = ball.y >>> 8, attribute = ball.attribute & 255;
    final int disabled = ball.disabledObjects;
    final boolean colliding = ball.colliding;
    switch (ball.stage) {
      case 0:
        if ((disabled & (1 << RED_VOLTORB)) == 0 && colliding && ((attribute >= 224 && attribute <= 245))) {
          if (((x + 203) & 255) < 27 && ((y + 167) & 255) < 27) { return (RED_VOLTORB << 8) | 3; }
          if (((x + 179) & 255) < 27 && ((y + 177) & 255) < 27) { return (RED_VOLTORB << 8) | 4; }
          if (((x + 184) & 255) < 27 && ((y + 149) & 255) < 27) { return (RED_VOLTORB << 8) | 5; }
        }
        if ((disabled & (1 << RED_SPINNER)) == 0) {
          if (((x + 119) & 255) < 15 && ((y + 151) & 255) < 7) { return (RED_SPINNER << 8) | 9; }
        }
        if ((disabled & (1 << RED_BOARD)) == 0) {
          if (((x + 236) & 255) < 17 && ((y + 204) & 255) < 17) { return (RED_BOARD << 8) | 17; }
          if (((x + 222) & 255) < 17 && ((y + 196) & 255) < 17) { return (RED_BOARD << 8) | 18; }
          if (((x + 227) & 255) < 17 && ((y + 165) & 255) < 17) { return (RED_BOARD << 8) | 19; }
          if (((x + 246) & 255) < 17 && ((y + 142) & 255) < 17) { return (RED_BOARD << 8) | 20; }
          if (((x + 226) & 255) < 17 && ((y + 132) & 255) < 17) { return (RED_BOARD << 8) | 21; }
          if (((x + 140) & 255) < 17 && ((y + 196) & 255) < 17) { return (RED_BOARD << 8) | 22; }
          if (((x + 122) & 255) < 17 && ((y + 142) & 255) < 17) { return (RED_BOARD << 8) | 23; }
          if (((x + 137) & 255) < 17 && ((y + 207) & 255) < 17) { return (RED_BOARD << 8) | 24; }
        }
        if ((disabled & (1 << RED_STARYU_TOP)) == 0 && colliding && ((attribute >= 16 && attribute <= 18))) {
          if (((x + 199) & 255) < 15 && ((y + 117) & 255) < 11) { return (RED_STARYU_TOP << 8) | 25; }
        }
        if ((disabled & (1 << RED_BELLSPROUT)) == 0) {
          if (((x + 138) & 255) < 11 && ((y + 142) & 255) < 9) { return (RED_BELLSPROUT << 8) | 27; }
        }
        if ((disabled & (1 << RED_DITTO)) == 0) {
          if (((x + 240) & 255) < 5 && ((y + 222) & 255) < 5) { return (RED_DITTO << 8) | 31; }
        }
        if ((disabled & (1 << RED_UPGRADE)) == 0) {
          if (((x + 203) & 255) < 11 && ((y + 177) & 255) < 9) { return (RED_UPGRADE << 8) | 14; }
          if (((x + 181) & 255) < 11 && ((y + 188) & 255) < 9) { return (RED_UPGRADE << 8) | 15; }
          if (((x + 159) & 255) < 11 && ((y + 187) & 255) < 9) { return (RED_UPGRADE << 8) | 16; }
        }
        return -1;
      case 1:
        if (y < 86) {
          if ((disabled & (1 << RED_WILD)) == 0 && colliding && ((attribute >= 208 && attribute <= 223))) {
            if (((x + 201) & 255) < 51 && ((y + 217) & 255) < 51) { return (RED_WILD << 8) | 30; }
          }
          if ((disabled & (1 << RED_STARYU_BOTTOM)) == 0 && colliding && (attribute == 86 || (attribute >= 91 && attribute <= 92))) {
            if (((x + 199) & 255) < 15 && ((y + 253) & 255) < 11) { return (RED_STARYU_BOTTOM << 8) | 26; }
          }
          if ((disabled & (1 << RED_DIGLETT)) == 0 && colliding && ((attribute >= 100 && attribute <= 101) || (attribute >= 104 && attribute <= 105))) {
            if (((x + 231) & 255) < 15 && ((y + 203) & 255) < 23) { return (RED_DIGLETT << 8) | 1; }
            if (((x + 135) & 255) < 15 && ((y + 203) & 255) < 23) { return (RED_DIGLETT << 8) | 2; }
          }
          if ((disabled & (1 << RED_MULTIPLIER)) == 0 && colliding && ((attribute >= 71 && attribute <= 72) || (attribute >= 74 && attribute <= 77))) {
            if (((x + 218) & 255) < 13 && ((y + 230) & 255) < 13) { return (RED_MULTIPLIER << 8) | 33; }
            if (((x + 146) & 255) < 13 && ((y + 230) & 255) < 13) { return (RED_MULTIPLIER << 8) | 34; }
          }
          if ((disabled & (1 << RED_SLOT)) == 0) {
            if (((x + 179) & 255) < 7 && ((y + 237) & 255) < 7) { return (RED_SLOT << 8) | 32; }
          }
        } else {
          if ((disabled & (1 << RED_BUMPERS)) == 0 && colliding && ((attribute >= 49 && attribute <= 50) || attribute == 52 || (attribute >= 54 && attribute <= 55) || (attribute >= 59 && attribute <= 63))) {
            if (((x + 213) & 255) < 11 && ((y + 164) & 255) < 21) { return (RED_BUMPERS << 8) | 6; }
            if (((x + 150) & 255) < 11 && ((y + 164) & 255) < 21) { return (RED_BUMPERS << 8) | 7; }
          }
          if ((disabled & (1 << RED_PIKACHU)) == 0) {
            if (((x + 244) & 255) < 5 && ((y + 136) & 255) < 9) { return (RED_PIKACHU << 8) | 28; }
            if (((x + 112) & 255) < 5 && ((y + 136) & 255) < 9) { return (RED_PIKACHU << 8) | 29; }
          }
          if ((disabled & (1 << RED_CAVE)) == 0) {
            if (((x + 246) & 255) < 9 && ((y + 157) & 255) < 5) { return (RED_CAVE << 8) | 10; }
            if (((x + 230) & 255) < 9 && ((y + 157) & 255) < 5) { return (RED_CAVE << 8) | 11; }
            if (((x + 130) & 255) < 9 && ((y + 157) & 255) < 5) { return (RED_CAVE << 8) | 12; }
            if (((x + 114) & 255) < 9 && ((y + 157) & 255) < 5) { return (RED_CAVE << 8) | 13; }
          }
          if ((disabled & (1 << RED_LAUNCH)) == 0) {
            if (((x + 95) & 255) < 15 && ((y + 111) & 255) < 15) { return (RED_LAUNCH << 8) | 8; }
          }
        }
        return -1;
      case 4:
        if ((disabled & (1 << BLUE_SHELLDER)) == 0 && colliding && ((attribute >= 94 && attribute <= 111) || (attribute >= 152 && attribute <= 153))) {
          if (((x + 211) & 255) < 27 && ((y + 183) & 255) < 27) { return (BLUE_SHELLDER << 8) | 3; }
          if (((x + 190) & 255) < 27 && ((y + 200) & 255) < 27) { return (BLUE_SHELLDER << 8) | 4; }
          if (((x + 169) & 255) < 27 && ((y + 183) & 255) < 27) { return (BLUE_SHELLDER << 8) | 5; }
        }
        if ((disabled & (1 << BLUE_SPINNER)) == 0) {
          if (((x + 117) & 255) < 15 && ((y + 153) & 255) < 7) { return (BLUE_SPINNER << 8) | 6; }
        }
        if ((disabled & (1 << BLUE_BOARD)) == 0) {
          if (((x + 243) & 255) < 17 && ((y + 197) & 255) < 17) { return (BLUE_BOARD << 8) | 7; }
          if (((x + 125) & 255) < 17 && ((y + 197) & 255) < 17) { return (BLUE_BOARD << 8) | 8; }
          if (((x + 248) & 255) < 17 && ((y + 124) & 255) < 17) { return (BLUE_BOARD << 8) | 9; }
          if (((x + 121) & 255) < 17 && ((y + 124) & 255) < 17) { return (BLUE_BOARD << 8) | 10; }
        }
        if ((disabled & (1 << BLUE_SLOWPOKE)) == 0) {
          if (((x + 217) & 255) < 11 && ((y + 140) & 255) < 9) { return (BLUE_SLOWPOKE << 8) | 12; }
        }
        if ((disabled & (1 << BLUE_CLOYSTER)) == 0) {
          if (((x + 146) & 255) < 11 && ((y + 140) & 255) < 9) { return (BLUE_CLOYSTER << 8) | 11; }
        }
        if ((disabled & (1 << BLUE_UPGRADE)) == 0) {
          if (((x + 206) & 255) < 11 && ((y + 208) & 255) < 9) { return (BLUE_UPGRADE << 8) | 19; }
          if (((x + 182) & 255) < 11 && ((y + 213) & 255) < 9) { return (BLUE_UPGRADE << 8) | 20; }
          if (((x + 158) & 255) < 11 && ((y + 207) & 255) < 9) { return (BLUE_UPGRADE << 8) | 21; }
        }
        return -1;
      case 5:
        if (y < 86) {
          if ((disabled & (1 << BLUE_WILD)) == 0 && colliding && ((attribute >= 208 && attribute <= 223))) {
            if (((x + 201) & 255) < 51 && ((y + 217) & 255) < 51) { return (BLUE_WILD << 8) | 27; }
          }
          if ((disabled & (1 << BLUE_PSYDUCK)) == 0 && colliding && ((attribute >= 36 && attribute <= 37) || (attribute >= 94 && attribute <= 95))) {
            if (((x + 229) & 255) < 15 && ((y + 205) & 255) < 23) { return (BLUE_PSYDUCK << 8) | 17; }
            if (((x + 138) & 255) < 15 && ((y + 206) & 255) < 23) { return (BLUE_PSYDUCK << 8) | 18; }
          }
          if ((disabled & (1 << BLUE_MULTIPLIER)) == 0 && colliding && ((attribute >= 28 && attribute <= 29) || (attribute >= 86 && attribute <= 93))) {
            if (((x + 220) & 255) < 17 && ((y + 231) & 255) < 15) { return (BLUE_MULTIPLIER << 8) | 15; }
            if (((x + 148) & 255) < 17 && ((y + 231) & 255) < 15) { return (BLUE_MULTIPLIER << 8) | 16; }
          }
          if ((disabled & (1 << BLUE_SLOT)) == 0) {
            if (((x + 179) & 255) < 7 && ((y + 237) & 255) < 7) { return (BLUE_SLOT << 8) | 26; }
          }
        } else {
          if ((disabled & (1 << BLUE_BUMPERS)) == 0 && colliding && ((attribute >= 48 && attribute <= 63))) {
            if (((x + 213) & 255) < 11 && ((y + 164) & 255) < 21) { return (BLUE_BUMPERS << 8) | 1; }
            if (((x + 150) & 255) < 11 && ((y + 164) & 255) < 21) { return (BLUE_BUMPERS << 8) | 2; }
          }
          if ((disabled & (1 << BLUE_PIKACHU)) == 0) {
            if (((x + 244) & 255) < 5 && ((y + 136) & 255) < 9) { return (BLUE_PIKACHU << 8) | 13; }
            if (((x + 112) & 255) < 5 && ((y + 136) & 255) < 9) { return (BLUE_PIKACHU << 8) | 14; }
          }
          if ((disabled & (1 << BLUE_CAVE)) == 0) {
            if (((x + 246) & 255) < 9 && ((y + 157) & 255) < 5) { return (BLUE_CAVE << 8) | 22; }
            if (((x + 230) & 255) < 9 && ((y + 157) & 255) < 5) { return (BLUE_CAVE << 8) | 23; }
            if (((x + 130) & 255) < 9 && ((y + 157) & 255) < 5) { return (BLUE_CAVE << 8) | 24; }
            if (((x + 114) & 255) < 9 && ((y + 157) & 255) < 5) { return (BLUE_CAVE << 8) | 25; }
          }
          if ((disabled & (1 << BLUE_LAUNCH)) == 0) {
            if (((x + 95) & 255) < 15 && ((y + 111) & 255) < 15) { return (BLUE_LAUNCH << 8) | 28; }
          }
        }
        return -1;
      default: return -1;
    }
  }
}

package probe;

public final class PokemonFieldSpriteData {
  private PokemonFieldSpriteData() {}
  @NoInline public static int redBumpers(final int slot, final int camera) {
      PokemonRender.fieldSprite(slot + 0, camera, 74, 94, 48, 17);
      PokemonRender.fieldSprite(slot + 1, camera, 66, 94, 46, 17);
      PokemonRender.fieldSprite(slot + 2, camera, 99, 84, 48, 17);
      PokemonRender.fieldSprite(slot + 3, camera, 91, 84, 46, 17);
      PokemonRender.fieldSprite(slot + 4, camera, 93, 112, 48, 17);
      PokemonRender.fieldSprite(slot + 5, camera, 85, 112, 46, 17);
      return slot + 6;
  }
  @NoInline public static int ditto(final int slot, final int camera, final int state) {
    switch (state) {
      case 0:
      case 1:
      case 2:
      case 3:
      PokemonRender.fieldSprite(slot + 0, camera, 33, 40, 44, 2);
      PokemonRender.fieldSprite(slot + 1, camera, 25, 40, 40, 2);
      PokemonRender.fieldSprite(slot + 2, camera, 9, 64, 32, 2);
      PokemonRender.fieldSprite(slot + 3, camera, 9, 48, 30, 2);
      PokemonRender.fieldSprite(slot + 4, camera, 25, 56, 42, 2);
      PokemonRender.fieldSprite(slot + 5, camera, 17, 64, 38, 2);
      PokemonRender.fieldSprite(slot + 6, camera, 17, 48, 36, 2);
      PokemonRender.fieldSprite(slot + 7, camera, 17, 32, 34, 2);
      PokemonRender.fieldSprite(slot + 8, camera, 9, 32, 28, 2);
      return slot + 9;
      case 4:
      case 5:
      PokemonRender.fieldSprite(slot + 0, camera, 17, 40, 150, 2);
      PokemonRender.fieldSprite(slot + 1, camera, 41, 48, 166, 2);
      PokemonRender.fieldSprite(slot + 2, camera, 33, 64, 164, 2);
      PokemonRender.fieldSprite(slot + 3, camera, 33, 48, 162, 2);
      PokemonRender.fieldSprite(slot + 4, camera, 25, 72, 160, 2);
      PokemonRender.fieldSprite(slot + 5, camera, 25, 56, 158, 2);
      PokemonRender.fieldSprite(slot + 6, camera, 25, 40, 156, 2);
      PokemonRender.fieldSprite(slot + 7, camera, 17, 72, 154, 2);
      PokemonRender.fieldSprite(slot + 8, camera, 17, 56, 152, 2);
      PokemonRender.fieldSprite(slot + 9, camera, 9, 72, 148, 2);
      PokemonRender.fieldSprite(slot + 10, camera, 9, 56, 146, 2);
      PokemonRender.fieldSprite(slot + 11, camera, 9, 40, 144, 2);
      return slot + 12;
      case 6:
      case 7:
      PokemonRender.fieldSprite(slot + 0, camera, 8, 64, 26, 2);
      PokemonRender.fieldSprite(slot + 1, camera, 8, 48, 168, 2);
      return slot + 2;
      default: return slot;
    }
  }
  @NoInline public static int redCreatures(final int slot, final int camera, final boolean color) {
    switch (color ? 1 : 0) {
      case 0:
      PokemonRender.fieldSprite(slot + 0, camera, 132, 100, 102, 21);
      PokemonRender.fieldSprite(slot + 1, camera, 132, 84, 100, 21);
      PokemonRender.fieldSprite(slot + 2, camera, 124, 100, 98, 21);
      PokemonRender.fieldSprite(slot + 3, camera, 124, 84, 96, 21);
      return slot + 4;
      case 1:
      PokemonRender.fieldSprite(slot + 0, camera, 132, 100, 102, 21);
      PokemonRender.fieldSprite(slot + 1, camera, 132, 84, 100, 21);
      PokemonRender.fieldSprite(slot + 2, camera, 124, 100, 98, 21);
      PokemonRender.fieldSprite(slot + 3, camera, 124, 84, 96, 21);
      PokemonRender.fieldSprite(slot + 4, camera, 120, 116, 14, 12);
      PokemonRender.fieldSprite(slot + 5, camera, 120, 100, 12, 12);
      PokemonRender.fieldSprite(slot + 6, camera, 112, 118, 10, 12);
      PokemonRender.fieldSprite(slot + 7, camera, 112, 102, 8, 12);
      PokemonRender.fieldSprite(slot + 8, camera, 67, 121, 6, 27);
      PokemonRender.fieldSprite(slot + 9, camera, 59, 137, 4, 27);
      PokemonRender.fieldSprite(slot + 10, camera, 59, 121, 2, 27);
      PokemonRender.fieldSprite(slot + 11, camera, 51, 121, 0, 27);
      return slot + 12;
      default: return slot;
    }
  }
  @NoInline public static int blueBumpers(final int slot, final int camera) {
      PokemonRender.fieldSprite(slot + 0, camera, 88, 61, 146, 19);
      PokemonRender.fieldSprite(slot + 1, camera, 80, 61, 144, 19);
      PokemonRender.fieldSprite(slot + 2, camera, 67, 78, 146, 19);
      PokemonRender.fieldSprite(slot + 3, camera, 59, 78, 144, 19);
      PokemonRender.fieldSprite(slot + 4, camera, 109, 78, 146, 19);
      PokemonRender.fieldSprite(slot + 5, camera, 101, 78, 144, 19);
      return slot + 6;
  }
  @NoInline public static int blueCreatures(final int slot, final int camera) {
      PokemonRender.fieldSprite(slot + 0, camera, 48, 127, 30, 17);
      PokemonRender.fieldSprite(slot + 1, camera, 40, 127, 28, 17);
      PokemonRender.fieldSprite(slot + 2, camera, 32, 127, 26, 17);
      PokemonRender.fieldSprite(slot + 3, camera, 48, 111, 156, 17);
      PokemonRender.fieldSprite(slot + 4, camera, 40, 111, 154, 17);
      PokemonRender.fieldSprite(slot + 5, camera, 32, 111, 152, 17);
      PokemonRender.fieldSprite(slot + 6, camera, 136, 121, 42, 19);
      PokemonRender.fieldSprite(slot + 7, camera, 128, 121, 40, 19);
      PokemonRender.fieldSprite(slot + 8, camera, 120, 121, 38, 19);
      PokemonRender.fieldSprite(slot + 9, camera, 136, 105, 36, 19);
      PokemonRender.fieldSprite(slot + 10, camera, 128, 105, 34, 19);
      PokemonRender.fieldSprite(slot + 11, camera, 120, 105, 32, 19);
      return slot + 12;
  }
  @NoInline public static int pikachu(final int slot, final int camera) {
      PokemonRender.fieldSprite(slot + 0, camera, 24, 126, 116, 4);
      PokemonRender.fieldSprite(slot + 1, camera, 16, 126, 114, 4);
      return slot + 2;
  }
  @NoInline public static int pikachuAnimated(final int slot, final int camera, final int side, final int pose) {
    switch (side*9+pose) {
      case 0:
        PokemonRender.fieldSprite(slot+0,camera,24,126,116,4);
        PokemonRender.fieldSprite(slot+1,camera,16,126,114,4);
        return slot+2;
      case 1:
        PokemonRender.fieldSprite(slot+0,camera,16,126,118,4);
        PokemonRender.fieldSprite(slot+1,camera,24,126,120,4);
        return slot+2;
      case 2:
        PokemonRender.fieldSprite(slot+0,camera,24,126,124,4);
        PokemonRender.fieldSprite(slot+1,camera,16,126,122,4);
        return slot+2;
      case 3:
        PokemonRender.fieldSprite(slot+0,camera,16,115,61,81);
        PokemonRender.fieldSprite(slot+1,camera,21,111,62,49);
        PokemonRender.fieldSprite(slot+2,camera,24,126,124,4);
        PokemonRender.fieldSprite(slot+3,camera,16,126,122,4);
        return slot+4;
      case 4:
        PokemonRender.fieldSprite(slot+0,camera,22,111,60,17);
        PokemonRender.fieldSprite(slot+1,camera,17,111,62,17);
        PokemonRender.fieldSprite(slot+2,camera,24,126,124,4);
        PokemonRender.fieldSprite(slot+3,camera,16,126,122,4);
        return slot+4;
      case 5:
        PokemonRender.fieldSprite(slot+0,camera,21,103,62,49);
        PokemonRender.fieldSprite(slot+1,camera,16,103,60,49);
        PokemonRender.fieldSprite(slot+2,camera,15,111,62,49);
        PokemonRender.fieldSprite(slot+3,camera,23,111,60,49);
        PokemonRender.fieldSprite(slot+4,camera,24,126,124,4);
        PokemonRender.fieldSprite(slot+5,camera,16,126,122,4);
        return slot+6;
      case 6:
        PokemonRender.fieldSprite(slot+0,camera,16,102,62,17);
        PokemonRender.fieldSprite(slot+1,camera,21,102,60,17);
        PokemonRender.fieldSprite(slot+2,camera,16,113,60,49);
        PokemonRender.fieldSprite(slot+3,camera,20,113,62,49);
        PokemonRender.fieldSprite(slot+4,camera,24,126,124,4);
        PokemonRender.fieldSprite(slot+5,camera,16,126,122,4);
        return slot+6;
      case 7:
        PokemonRender.fieldSprite(slot+0,camera,24,99,62,17);
        PokemonRender.fieldSprite(slot+1,camera,22,111,60,17);
        PokemonRender.fieldSprite(slot+2,camera,17,103,63,81);
        PokemonRender.fieldSprite(slot+3,camera,16,112,60,49);
        PokemonRender.fieldSprite(slot+4,camera,24,126,124,4);
        PokemonRender.fieldSprite(slot+5,camera,16,126,122,4);
        return slot+6;
      case 8:
        PokemonRender.fieldSprite(slot+0,camera,16,102,61,81);
        PokemonRender.fieldSprite(slot+1,camera,15,115,63,113);
        PokemonRender.fieldSprite(slot+2,camera,21,112,62,49);
        PokemonRender.fieldSprite(slot+3,camera,22,100,60,17);
        PokemonRender.fieldSprite(slot+4,camera,24,126,124,4);
        PokemonRender.fieldSprite(slot+5,camera,16,126,122,4);
        return slot+6;
      case 9:
        PokemonRender.fieldSprite(slot+0,camera,155,126,116,4);
        PokemonRender.fieldSprite(slot+1,camera,147,126,114,4);
        return slot+2;
      case 10:
        PokemonRender.fieldSprite(slot+0,camera,147,126,118,4);
        PokemonRender.fieldSprite(slot+1,camera,155,126,120,4);
        return slot+2;
      case 11:
        PokemonRender.fieldSprite(slot+0,camera,155,126,124,4);
        PokemonRender.fieldSprite(slot+1,camera,147,126,122,4);
        return slot+2;
      case 12:
        PokemonRender.fieldSprite(slot+0,camera,147,115,61,81);
        PokemonRender.fieldSprite(slot+1,camera,152,111,62,49);
        PokemonRender.fieldSprite(slot+2,camera,155,126,124,4);
        PokemonRender.fieldSprite(slot+3,camera,147,126,122,4);
        return slot+4;
      case 13:
        PokemonRender.fieldSprite(slot+0,camera,153,111,60,17);
        PokemonRender.fieldSprite(slot+1,camera,148,111,62,17);
        PokemonRender.fieldSprite(slot+2,camera,155,126,124,4);
        PokemonRender.fieldSprite(slot+3,camera,147,126,122,4);
        return slot+4;
      case 14:
        PokemonRender.fieldSprite(slot+0,camera,152,103,62,49);
        PokemonRender.fieldSprite(slot+1,camera,147,103,60,49);
        PokemonRender.fieldSprite(slot+2,camera,146,111,62,49);
        PokemonRender.fieldSprite(slot+3,camera,154,111,60,49);
        PokemonRender.fieldSprite(slot+4,camera,155,126,124,4);
        PokemonRender.fieldSprite(slot+5,camera,147,126,122,4);
        return slot+6;
      case 15:
        PokemonRender.fieldSprite(slot+0,camera,147,102,62,17);
        PokemonRender.fieldSprite(slot+1,camera,152,102,60,17);
        PokemonRender.fieldSprite(slot+2,camera,147,113,60,49);
        PokemonRender.fieldSprite(slot+3,camera,151,113,62,49);
        PokemonRender.fieldSprite(slot+4,camera,155,126,124,4);
        PokemonRender.fieldSprite(slot+5,camera,147,126,122,4);
        return slot+6;
      case 16:
        PokemonRender.fieldSprite(slot+0,camera,155,99,62,17);
        PokemonRender.fieldSprite(slot+1,camera,153,111,60,17);
        PokemonRender.fieldSprite(slot+2,camera,148,103,63,81);
        PokemonRender.fieldSprite(slot+3,camera,147,112,60,49);
        PokemonRender.fieldSprite(slot+4,camera,155,126,124,4);
        PokemonRender.fieldSprite(slot+5,camera,147,126,122,4);
        return slot+6;
      case 17:
        PokemonRender.fieldSprite(slot+0,camera,147,102,61,81);
        PokemonRender.fieldSprite(slot+1,camera,146,115,63,113);
        PokemonRender.fieldSprite(slot+2,camera,152,112,62,49);
        PokemonRender.fieldSprite(slot+3,camera,153,100,60,17);
        PokemonRender.fieldSprite(slot+4,camera,155,126,124,4);
        PokemonRender.fieldSprite(slot+5,camera,147,126,122,4);
        return slot+6;
      default: GameBoyHardware.trap(2); return slot;
    }
  }
}

package probe;

import static probe.PokemonConstants.*;

public final class PokemonRender {
  private PokemonRender() {}
  private static final int BALL_FIRST_TILE = 0x40, BALL_FRAME_TILES = 4;
  private static final int FLIPPER_FIRST_TILE = 0x60, FLIPPER_FRAME_TILES = 6;
  private static final int FLIPPER_PALETTE = 2, MIRROR_X = 0x20;
  private static final int SPINNER_RED_X = 136, SPINNER_BLUE_X = 138;
  private static final int SPINNER_RED_Y = 90, SPINNER_BLUE_Y = 83;
  private static byte paintedStage = -1, paintedCamera, paintedState, ballSlot, spinnerSlot;
  private static boolean paintedColor;
  private static byte saverSide, saverPose, paintedSaverSide = -1, paintedSaverPose = -1, flipperBase;
  private static byte paintedLeft = -1, paintedRight = -1, paintedFlipperCamera = -1;
  private static byte paintedSpinner = -1, paintedSpinnerCamera = -1;

  public static void saver(final int side, final int pose) {
    saverSide = (byte) side;
    saverPose = (byte) pose;
  }

  public static void invalidate() {
    paintedStage = -1;
    paintedLeft = -1;
    paintedRight = -1;
    paintedSpinner = -1;
  }

  private static int flipperFrame(final int value) {
    return value < FLIPPER_MIDDLE ? 0 : value < FLIPPER_UP ? 1 : 2;
  }

  private static int pack(final int x, final int y, final int tile, final int flags) {
    return (y & BYTE_MASK) | ((x & BYTE_MASK) << 8) | ((tile & BYTE_MASK) << 16) | (flags << 24);
  }

  static void fieldSprite(final int slot, final int camera, final int x, final int y, final int tile, final int flags) {
    PokemonHardware.oamPacked(slot, pack(x - camera, y, tile, flags));
  }

  @NoInline
  private static void field(final PokemonBall ball, final int camera, final boolean color) {
    if (paintedStage == ball.stage && paintedCamera == camera
        && paintedState == ball.collisionState && paintedColor == color
        && ((ball.stage & 1) == 0 || (paintedSaverSide == saverSide && paintedSaverPose == saverPose))) {
      return;
    }
    paintedLeft = -1;
    paintedRight = -1;
    paintedSpinner = -1;
    if ((ball.stage & 1) != 0) {
      flipperBase = (byte) PokemonFieldSpriteData.pikachuAnimated(0,camera,saverSide,saverPose);
      ballSlot = (byte) (flipperBase + 6);
      paintedSaverSide = saverSide;
      paintedSaverPose = saverPose;
    } else if (ball.stage == RED_TOP) {
      final int bumpers = PokemonFieldSpriteData.redBumpers(0, camera);
      final int ditto = PokemonFieldSpriteData.ditto(bumpers, camera, ball.collisionState);
      spinnerSlot = (byte) PokemonFieldSpriteData.redCreatures(ditto, camera, color);
      ballSlot = (byte) (spinnerSlot + 2);
    } else {
      spinnerSlot = (byte) PokemonFieldSpriteData.blueBumpers(0, camera);
      ballSlot = (byte) PokemonFieldSpriteData.blueCreatures(spinnerSlot + 2, camera);
    }
    GameBoy.hideSprites(ballSlot + 2, 40);
    paintedStage = ball.stage;
    paintedCamera = (byte) camera;
    paintedState = ball.collisionState;
    paintedColor = color;
  }

  @NoInline
  private static void flipper(final int slot, final int frame, final int x, final boolean left) {
    final int tile = frame == 0 ? FLIPPER_FIRST_TILE : frame == 1 ? FLIPPER_FIRST_TILE + FLIPPER_FRAME_TILES : FLIPPER_FIRST_TILE + FLIPPER_FRAME_TILES * 2;
    final int flags = left ? FLIPPER_PALETTE | MIRROR_X : FLIPPER_PALETTE;
    PokemonHardware.oamPacked(slot, pack(x + (left ? 3 : 5), FLIPPER_ANCHOR_Y + (frame == 2 ? 10 : 12), tile + 4, flags));
    PokemonHardware.oamPacked(slot + 1, pack(x + (left ? 11 : -3), FLIPPER_ANCHOR_Y + (frame == 0 ? 10 : frame == 1 ? 4 : 3), tile + 2, flags));
    PokemonHardware.oamPacked(slot + 2, pack(x + (left ? 19 : -11), FLIPPER_ANCHOR_Y + (frame == 0 ? 20 : frame == 1 ? 12 : -3), tile, flags));
  }

  @NoInline
  public static void draw(final PokemonBall ball, final int cameraX, final int spinnerState, final boolean color) {
    field(ball, cameraX, color);
    final int frame = (ball.rotation >>> BALL_FRAME_SHIFT) & (BALL_FRAMES - 1);
    final int tile = BALL_FIRST_TILE + frame * BALL_FRAME_TILES;
    final int x = (ball.x >>> FRACTION_BITS) + 1 - cameraX, y = (ball.y >>> FRACTION_BITS) + 1 - TILE_SIZE;
    PokemonHardware.oamPacked(ballSlot, pack(x + TILE_SIZE, y, tile + 2, 0));
    PokemonHardware.oamPacked(ballSlot + 1, pack(x, y, tile, 0));
    if ((ball.stage & 1) != 0) {
      final int left = flipperFrame(ball.left >>> FRACTION_BITS), right = flipperFrame(ball.right >>> FRACTION_BITS);
      if (paintedLeft != left || paintedFlipperCamera != cameraX) {
        flipper(flipperBase, left, LEFT_FLIPPER_ANCHOR - cameraX, true);
        paintedLeft = (byte) left;
      }
      if (paintedRight != right || paintedFlipperCamera != cameraX) {
        flipper(flipperBase + 3, right, RIGHT_FLIPPER_ANCHOR - cameraX, false);
        paintedRight = (byte) right;
      }
      paintedFlipperCamera = (byte) cameraX;
    } else {
      final int spinnerFrame = spinnerState >>> 10;
      if (paintedSpinner == spinnerFrame && paintedSpinnerCamera == cameraX) {
        PokemonHardware.scroll(cameraX, 0);
        return;
      }
      paintedSpinner = (byte) spinnerFrame;
      paintedSpinnerCamera = (byte) cameraX;
      final boolean blue = ball.stage == BLUE_TOP;
      final int spinnerTile = spinnerFrame == 0 ? 120 : spinnerFrame == 1 ? 123 : spinnerFrame == 2 ? 125
          : spinnerFrame == 3 ? 126 : spinnerFrame == 4 ? 124 : 122;
      final int flags = spinnerFrame == 1 || spinnerFrame == 2 ? 0x42 : 0x02;
      final int spinnerX = (blue ? SPINNER_BLUE_X + 8 : SPINNER_RED_X + 7) - cameraX;
      final int spinnerY = blue ? SPINNER_BLUE_Y + 16 : SPINNER_RED_Y + 8;
      PokemonHardware.oamPacked(spinnerSlot, pack(spinnerX + TILE_SIZE, spinnerY, spinnerTile, flags | MIRROR_X));
      PokemonHardware.oamPacked(spinnerSlot + 1, pack(spinnerX, spinnerY, spinnerTile, flags));
    }
    PokemonHardware.scroll(cameraX, 0);
  }
}

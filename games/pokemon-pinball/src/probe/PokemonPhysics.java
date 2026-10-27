package probe;
import dev.java2gb.api.*;

import static probe.PokemonConstants.*;

public final class PokemonPhysics {
  private PokemonPhysics() {}

  public static short limit(final int velocity) {
    final int high = (short) velocity >> FRACTION_BITS;
    return (short) (high >= 8 ? (7 << FRACTION_BITS) | (velocity & BYTE_MASK)
        : high < -7 ? (-7 << FRACTION_BITS) | (velocity & BYTE_MASK) : velocity);
  }

  public static int displacement(final int velocity) {
    final int high = (short) velocity >> FRACTION_BITS;
    return high >= 5 ? 0x4ff : high < -4 ? -0x4ff : (short) velocity;
  }

  public static int sine(final int angle) {
    int index = angle & 127;
    if (index >= 64) {
      index = 128 - index;
    }
    final int magnitude = PokemonData.sineMagnitude(index);
    return (angle & 128) == 0 ? magnitude : -magnitude;
  }

  public static short scale(final int component, final int factor) {
    final int velocity = (short) component;
    final int positiveVelocity = velocity < 0 ? -velocity : velocity;
    final int positiveFactor = factor < 0 ? -factor : factor;
    final char low = (char) ((positiveVelocity & BYTE_MASK) * (positiveFactor & BYTE_MASK));
    final char high = (char) ((positiveVelocity >>> FRACTION_BITS) * (positiveFactor & BYTE_MASK));
    final int result = (char) (high + ((char) (low + 128) >>> FRACTION_BITS));
    return (short) ((velocity < 0) != (factor < 0) ? -result : result);
  }

  @NoInline
  public static int rotate(final int vx, final int vy, final int angle) {
    final int sin = sine(angle), cos = sine(angle + 64);
    final short x = (short) (scale(vx, cos) + scale(vy, sin));
    final short y = (short) (scale(vy, cos) + scale(vx, -sin));
    return (x & WORD_MASK) | (y << 16);
  }

  public static int flipperChange(final int state, final boolean pressed) {
    final int change = pressed ? FLIPPER_STEP : -FLIPPER_STEP;
    final int high = state >>> FRACTION_BITS;
    return (high == 0 && change < 0) || (high == FLIPPER_LAST && change >= 0) ? 0 : change;
  }

  public static char flipperState(final int state, final int change) {
    final int next = state + change;
    return (char) (next < 0 ? 0 : next >= FLIPPER_LIMIT ? FLIPPER_CLAMP : next);
  }

  @NoInline
  private static PokemonMaskTile maskTile(final PokemonBall ball, final int attribute) {
    if (attribute == 0) { return ball.emptyMask; }
    if (attribute == 1) { return ball.fullMask; }
    final boolean lower = (ball.stage & 1) != 0;
    int frame = 0;
    if (lower && attribute >= FLIPPER_MASK_FIRST) {
      frame = (attribute & RIGHT_MASK_FLAG) != 0 ? (ball.maskFlippers >>> 2) & 3 : ball.maskFlippers & 3;
    } else if (lower && attribute >= ANIMATED_MASK_FIRST) {
      return ball.emptyMask;
    }
    final char key = (char) (attribute | (frame << 8) | ball.maskContext);
    if (ball.mask0.key == key) { return ball.mask0; }
    if (ball.mask1.key == key) { return ball.mask1; }
    if (ball.mask2.key == key) { return ball.mask2; }
    if (ball.mask3.key == key) { return ball.mask3; }
    final PokemonMaskTile tile = ball.nextMask == 0 ? ball.mask0 : ball.nextMask == 1 ? ball.mask1
        : ball.nextMask == 2 ? ball.mask2 : ball.mask3;
    ball.nextMask = (byte) ((ball.nextMask + 1) & 3);
    if (lower && attribute >= FLIPPER_MASK_FIRST) {
      final boolean right = (attribute & RIGHT_MASK_FLAG) != 0;
      final int index = frame * MASK_FRAME_BYTES + (attribute & MASK_ID_MASK) * TILE_SIZE;
      tile.low = PokemonData.lowerMask4(right,index);
      tile.high = PokemonData.lowerMask4(right,index + 4);
    } else {
      final int index = attribute * TILE_SIZE;
      tile.low = PokemonData.mask4(ball.stage,ball.collisionState,index);
      tile.high = PokemonData.mask4(ball.stage,ball.collisionState,index + 4);
    }
    tile.key = key;
    return tile;
  }

  private static int attribute(final PokemonBall ball, final int x, final int y) {
    return (x < TILE_SIZE ? (y < TILE_SIZE ? ball.attrUL : ball.attrLL)
        : (y < TILE_SIZE ? ball.attrUR : ball.attrLR)) & BYTE_MASK;
  }

  private static int maskRow(final PokemonBall ball, final int y) {
    return ball.maskRows[y];
  }

  @Mutable
  private static void cacheRows(final PokemonBall ball, final int row, final int left, final int right) {
    ball.maskRows[row] = (char) (((left & BYTE_MASK) << TILE_SIZE) | (right & BYTE_MASK));
    ball.maskRows[row + 1] = (char) (((left & 0xff00)) | ((right >>> 8) & BYTE_MASK));
    ball.maskRows[row + 2] = (char) (((left >>> 8) & 0xff00) | ((right >>> 16) & BYTE_MASK));
    ball.maskRows[row + 3] = (char) (((left >>> 16) & 0xff00) | ((right >>> 24) & BYTE_MASK));
  }

  @NoInline
  private static int perimeter(final PokemonBall ball, final int x, final int y) {
    final int shift = TILE_SIZE + 3 - x;
    final char middle = (char) ((char) maskRow(ball, y) >>> shift);
    int points = ((middle & 1) != 0 ? 1 : 0) | ((middle & 256) != 0 ? 256 : 0);
    final char southOne = (char) ((char) maskRow(ball, y + 1) >>> shift);
    points |= ((southOne & 1) != 0 ? 2 : 0) | ((southOne & 256) != 0 ? 128 : 0);
    final char southThree = (char) ((char) maskRow(ball, y + 3) >>> shift);
    points |= ((southThree & 2) != 0 ? 4 : 0) | ((southThree & 128) != 0 ? 64 : 0);
    final char southFour = (char) ((char) maskRow(ball, y + 4) >>> shift);
    points |= ((southFour & 8) != 0 ? 8 : 0) | ((southFour & 16) != 0 ? 16 : 0) | ((southFour & 32) != 0 ? 32 : 0);
    final char northOne = (char) ((char) maskRow(ball, y - 1) >>> shift);
    points |= ((northOne & 256) != 0 ? 512 : 0) | ((northOne & 1) != 0 ? 32768 : 0);
    final char northThree = (char) ((char) maskRow(ball, y - 3) >>> shift);
    points |= ((northThree & 128) != 0 ? 1024 : 0) | ((northThree & 2) != 0 ? 16384 : 0);
    final char northFour = (char) ((char) maskRow(ball, y - 4) >>> shift);
    points |= ((northFour & 32) != 0 ? 2048 : 0) | ((northFour & 16) != 0 ? 4096 : 0) | ((northFour & 8) != 0 ? 8192 : 0);
    return points;
  }

  private static char nextContact(final char points) {
    return (char) ((points >>> 1) | (points << (COLLISION_POINTS - 1)));
  }

  @NoInline
  static int contactKey(final int points) {
    char remaining = (char) points;
    int first = 0;
    while ((remaining & 1) != 0) {
      first++;
      remaining = nextContact(remaining);
    }
    int length = 0, start = 0;
    for (int i = first; i <= COLLISION_POINTS; i++) {
      if ((remaining & 1) == 0) {
        remaining = nextContact(remaining);
        continue;
      }
      final int begin = i;
      int count = 0;
      while ((remaining & 1) != 0) {
        count++;
        i++;
        remaining = nextContact(remaining);
      }
      if (count >= length) {
        length = count;
        start = begin;
      }
      if (i >= COLLISION_POINTS - 1) { break; }
      remaining = nextContact(remaining);
    }
    return (((start << 4) | (start >>> 4)) & BYTE_MASK) | ((start + length - 1) & COLLISION_POINT_MASK);
  }

  @NoInline
  public static void surface(final PokemonBall ball) {
    final int x = ball.x >>> FRACTION_BITS, y = ball.y >>> FRACTION_BITS;
    final int bx = (x - 4) & BYTE_MASK, by = (y - 4) & BYTE_MASK;
    final int index = (by >>> TILE_BITS) * MAP_STRIDE + (bx >>> TILE_BITS);
    final char attributeKey = (char) (index | (ball.stage << 10) | (ball.collisionState << 13));
    if (ball.attributeKey != attributeKey) {
      ball.attrUL = (byte) PokemonData.attribute(ball.stage, ball.collisionState, index);
      ball.attrLL = (byte) PokemonData.attribute(ball.stage, ball.collisionState, index + MAP_STRIDE);
      ball.attrUR = (byte) PokemonData.attribute(ball.stage, ball.collisionState, index + 1);
      ball.attrLR = (byte) PokemonData.attribute(ball.stage, ball.collisionState, index + MAP_STRIDE + 1);
      ball.attributeKey = attributeKey;
    }
    byte flipperKey = 0;
    if ((ball.stage & 1) != 0 && ((ball.attrUL & BYTE_MASK) >= FLIPPER_MASK_FIRST
        || (ball.attrLL & BYTE_MASK) >= FLIPPER_MASK_FIRST || (ball.attrUR & BYTE_MASK) >= FLIPPER_MASK_FIRST
        || (ball.attrLR & BYTE_MASK) >= FLIPPER_MASK_FIRST)) {
      final int left = ball.left >>> FRACTION_BITS, right = ball.right >>> FRACTION_BITS;
      final int leftFrame = left < FLIPPER_MIDDLE ? 0 : left < FLIPPER_UP ? 1 : 2;
      final int rightFrame = right < FLIPPER_MIDDLE ? 0 : right < FLIPPER_UP ? 1 : 2;
      flipperKey = (byte) (leftFrame | (rightFrame << 2));
    }
    if (ball.maskKey != attributeKey || ball.maskFlippers != flipperKey) {
      ball.maskFlippers = flipperKey;
      ball.maskContext = (char) ((ball.collisionState << 10) | (ball.stage << 13));
      if (emptyOrFull(ball)) {
        ball.maskKey = attributeKey;
        return;
      }
      if (ball.rowContext != ball.maskContext || ball.rowFlippers != flipperKey
          || ball.rowUL != ball.attrUL || ball.rowUR != ball.attrUR
          || ball.rowLL != ball.attrLL || ball.rowLR != ball.attrLR) {
        final PokemonMaskTile upperLeft = maskTile(ball,ball.attrUL & BYTE_MASK);
        final int upperLow = upperLeft.low, upperHigh = upperLeft.high;
        final PokemonMaskTile upperRight = maskTile(ball,ball.attrUR & BYTE_MASK);
        cacheRows(ball,0,upperLow,upperRight.low);
        cacheRows(ball,4,upperHigh,upperRight.high);
        final PokemonMaskTile lowerLeft = maskTile(ball,ball.attrLL & BYTE_MASK);
        final int lowerLow = lowerLeft.low, lowerHigh = lowerLeft.high;
        final PokemonMaskTile lowerRight = maskTile(ball,ball.attrLR & BYTE_MASK);
        cacheRows(ball,8,lowerLow,lowerRight.low);
        cacheRows(ball,12,lowerHigh,lowerRight.high);
        ball.rowContext = ball.maskContext;
        ball.rowFlippers = flipperKey;
        ball.rowUL = ball.attrUL;
        ball.rowUR = ball.attrUR;
        ball.rowLL = ball.attrLL;
        ball.rowLR = ball.attrLR;
      }
      ball.maskKey = attributeKey;
    }
    if (emptyOrFull(ball)) {
      return;
    }
    final int localX = (bx & TILE_MASK) + 4, localY = (by & TILE_MASK) + 4;
    final int points = perimeter(ball, localX, localY);
    if (points == 0 || points == WORD_MASK) {
      return;
    }
    final int contactKey;
    if (ball.lastContactValid && ball.lastContactPoints == points) {
      contactKey = ball.lastContactKey & BYTE_MASK;
    } else {
      contactKey = contactKey(points);
      ball.lastContactPoints = (char) points;
      ball.lastContactKey = (byte) contactKey;
      ball.lastContactValid = true;
    }
    ball.colliding = true;
    ball.normal = (byte) PokemonData.normal(contactKey);
    ball.x = (char) (ball.x + PokemonData.deltaX(contactKey));
    ball.y = (char) (ball.y + PokemonData.deltaY(contactKey));
    final int runStart = contactKey >>> 4, runEnd = contactKey & COLLISION_POINT_MASK;
    final int middle = (runStart + ((((runEnd - runStart) & COLLISION_POINT_MASK) + 1) >>> 1)) & COLLISION_POINT_MASK;
    final int px = localX + PokemonData.pointX(middle), py = localY + PokemonData.pointY(middle);
    ball.attribute = (byte) attribute(ball, px, py);
    ball.contacts++;
  }

  private static boolean emptyOrFull(final PokemonBall ball) {
    return (ball.attrUL | ball.attrLL | ball.attrUR | ball.attrLR) == 0
        || (ball.attrUL == 1 && ball.attrLL == 1 && ball.attrUR == 1 && ball.attrLR == 1);
  }

  @NoInline
  private static void flipper(final PokemonBall ball, final boolean right, final int previous, final int change) {
    final int bx = right ? ((FIELD_MIRROR_X << FRACTION_BITS) - ball.x) & WORD_MASK : ball.x;
    final int x = (bx >>> FRACTION_BITS) - FLIPPER_X;
    final int y = (ball.y >>> FRACTION_BITS) - FLIPPER_Y;
    if (x < 0 || x >= FLIPPER_WIDTH || y < 0 || y >= FLIPPER_HEIGHT) {
      return;
    }
    final int current = (right ? ball.right : ball.left) >>> FRACTION_BITS;
    int state = previous;
    while (true) {
      final int index = state * FLIPPER_STATE_BYTES + x * FLIPPER_HEIGHT + y;
      final int radius = PokemonData.flipperRadius(index);
      if (radius != 0) {
        final int angle = PokemonData.flipperAngle(index);
        ball.normal = (byte) (right ? -angle : angle);
        ball.flipperForce = (short) ((PokemonData.radiusMagnitude(radius) * (change * 4)) >> FRACTION_BITS);
        if (ball.flipperForce < 0) {
          ball.flipperForce = 0;
        }
        ball.amplification = 1;
        ball.flipperCollision = true;
        ball.colliding = true;
        ball.flipperHits++;
        return;
      }
      if (state == current) {
        return;
      }
      state += state < current ? 1 : -1;
    }
  }

  @NoInline
  public static void flippers(final PokemonBall ball, final int buttons) {
    final int leftChange = flipperChange(ball.left, (buttons & GameBoy.LEFT) != 0);
    final int rightChange = flipperChange(ball.right, (buttons & GameBoy.A) != 0);
    final int oldLeft = ball.left >>> FRACTION_BITS, oldRight = ball.right >>> FRACTION_BITS;
    ball.left = flipperState(ball.left, leftChange);
    ball.right = flipperState(ball.right, rightChange);
    if ((ball.x >>> FRACTION_BITS) < FIELD_CENTER_X) {
      flipper(ball, false, oldLeft, leftChange);
    } else {
      flipper(ball, true, oldRight, rightChange);
    }
  }

  @NoInline
  public static void rebound(final PokemonBall ball) {
    final int angle = ball.normal & BYTE_MASK;
    final int vector = rotate(ball.vx, ball.vy, angle);
    short x = (short) vector, y = (short) (vector >>> 16);
    if (y >= 0) {
      final int vertical = y;
      y = (short) -((vertical >>> 2) + (vertical >>> 3) * ball.amplification);
      x = (short) (x + (ball.spin >> 1));
      ball.spin = (byte) ((short) (x << 2) >> FRACTION_BITS);
    } else if (!ball.flipperCollision) {
      return;
    }
    if (ball.flipperCollision) {
      y -= ball.flipperForce;
    }
    final int back = rotate(x, y, -angle);
    ball.vx = (short) back;
    ball.vy = (short) (back >>> 16);
  }

  @NoInline
  public static void begin(final PokemonBall ball, final int buttons) {
    ball.colliding = false;
    ball.flipperCollision = false;
    ball.flipperForce = 0;
    ball.amplification = 0;
    if (ball.gravity) {
      ball.vy += GRAVITY;
    }
    ball.vx = limit(ball.vx);
    ball.vy = limit(ball.vy);
    if ((ball.stage & 1) != 0) {
      flippers(ball, buttons);
    }
    final byte flipperNormal = ball.normal;
    surface(ball);
    if (ball.flipperCollision) {
      ball.normal = flipperNormal;
    }
  }

  @NoInline
  public static void finish(final PokemonBall ball) {
    if (ball.colliding) {
      rebound(ball);
    }
    ball.x = (char) (ball.x + displacement(ball.vx));
    ball.y = (char) (ball.y + displacement(ball.vy));
    ball.rotation += ball.spin;
  }
}

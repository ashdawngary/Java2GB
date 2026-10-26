package probe;

import static probe.PokemonConstants.*;

public final class PokemonBall {
  public char x, y, left, right;
  public short vx, vy, flipperForce;
  public byte stage, collisionState, spin, rotation, normal, attribute, amplification;
  public byte previousObject = -1;
  public boolean gravity, colliding, flipperCollision, launched;
  public int contacts, flipperHits, bumperHits;
  public int disabledObjects;
  public char maskKey = WORD_MASK, attributeKey = WORD_MASK;
  public byte maskFlippers = -1;
  final PokemonMaskTile mask0 = new PokemonMaskTile(0,0), mask1 = new PokemonMaskTile(0,0);
  final PokemonMaskTile mask2 = new PokemonMaskTile(0,0), mask3 = new PokemonMaskTile(0,0);
  final PokemonMaskTile emptyMask = new PokemonMaskTile(0,0), fullMask = new PokemonMaskTile(-1,-1);
  char maskContext, rowContext = WORD_MASK;
  byte rowUL, rowUR, rowLL, rowLR, rowFlippers;
  byte nextMask;
  public final char[] maskRows = new char[TILE_SIZE * 2];
  public byte attrUL, attrLL, attrUR, attrLR;
  public char lastContactPoints;
  public byte lastContactKey;
  public boolean lastContactValid;

  public void ready(final boolean blue) {
    stage = (byte) (blue ? BLUE_BOTTOM : RED_BOTTOM);
    collisionState = (byte) (blue ? 0 : collisionState & 1);
    x = (char) (LAUNCH_X << FRACTION_BITS);
    y = (char) (LAUNCH_Y << FRACTION_BITS);
    vx = 0;
    vy = 0;
    spin = 0;
    rotation = 0;
    left = 0;
    right = 0;
    gravity = false;
    launched = false;
    colliding = false;
    flipperCollision = false;
    previousObject = -1;
    disabledObjects = 0;
    maskKey = WORD_MASK;
    attributeKey = WORD_MASK;
    maskFlippers = -1;
    lastContactValid = false;
  }

  public void launch() {
    vy = (short) (stage == BLUE_BOTTOM ? BLUE_LAUNCH_SPEED : RED_LAUNCH_SPEED);
    vx = 0;
    spin = 0;
    rotation = 0;
    gravity = true;
    launched = true;
  }
}

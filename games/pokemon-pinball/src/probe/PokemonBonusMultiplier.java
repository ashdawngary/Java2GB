package probe;

public final class PokemonBonusMultiplier {
  public static final int MAXIMUM = 99, EXTRA_INTERVAL = 25, COOLDOWN = 128, FAST_BLINK_AT = 112;
  public static final int UNLIT = 0, LIT = 1, FAST = 2, READY = 3, FLASH = 128;
  public byte value = 1, left = READY, right, delay, tens, ones = 2, endLight, endMask;
  public byte messageTens, messageOnes;
  public boolean message, extraBall;

  public void reset() {
    value = 1;
    left = READY;
    right = UNLIT;
    delay = 0;
    tens = 0;
    ones = 2;
    endLight = 0;
    endMask = 0;
    message = false;
    extraBall = false;
  }

  private void nextDigits() {
    final int next = value == MAXIMUM ? MAXIMUM : value + 1;
    tens = (byte) (next / 10);
    ones = (byte) (next % 10);
  }

  private static byte blink(final int digit, final int state, final int frame) {
    if (state < FAST) {
      return (byte) digit;
    }
    final int clock = state == READY ? frame >>> 2 : frame;
    if ((clock & 3) != 0) {
      return (byte) digit;
    }
    return (byte) ((digit & ~FLASH) | ((clock & 8) != 0 ? FLASH : 0));
  }

  @NoInline
  public void tick(final boolean blue, final int side, final int frame) {
    extraBall = false;
    if (blue) {
      endLight--;
      if (endLight == 0) { endMask = 0; }
    }
    if (side >= 0) {
      if (side == 0 && left == READY) {
        left = LIT;
        right = READY;
        tens |= FLASH;
      } else if (side == 1 && right == READY) {
        left = LIT;
        right = LIT;
        delay = (byte) COOLDOWN;
        ones |= FLASH;
        if (value != MAXIMUM) {
          value++;
          extraBall = value % EXTRA_INTERVAL == 0;
        }
        messageTens = tens;
        messageOnes = ones;
        message = true;
      }
      if (blue) {
        endLight = 60;
        endMask |= 1 << side;
      }
      return;
    }
    if (delay != 0) {
      delay--;
      if ((delay & 255) == FAST_BLINK_AT) {
        left = FAST;
        right = FAST;
      } else if (delay == 0) {
        left = READY;
        right = UNLIT;
        nextDigits();
        return;
      }
    }
    tens = blink(tens & 255, left, frame);
    ones = blink(ones & 255, right, frame);
  }
}

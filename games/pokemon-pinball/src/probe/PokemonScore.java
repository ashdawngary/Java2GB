package probe;
import dev.java2gb.api.*;

public final class PokemonScore {
  public static final int BYTES = 6;
  public static final int QUEUE_BYTES = BYTES * 16;
  public static final int FIVE = 0x05, TEN = 0x10, HUNDRED = 0x0100, FOUR_HUNDRED = 0x0400;
  public static final int FIVE_HUNDRED = 0x0500, THOUSAND = 0x1000, FIVE_THOUSAND = 0x5000;
  public static final int MILLION = 0x01000000, TEN_MILLION = 0x10000000;
  private final byte[] value = new byte[BYTES];
  private final byte[] pending = new byte[QUEUE_BYTES];
  private byte readOffset, writeOffset, idleEnd;
  public char version;

  @Mutable
  public void clear() {
    for (int i = 0; i < BYTES; i++) {
      value[i] = 0;
    }
    for (int i = 0; i < QUEUE_BYTES; i++) {
      pending[i] = 0;
    }
    readOffset = 0;
    writeOffset = 0;
    idleEnd = 0;
    version++;
  }

  public int byteAt(final int index) {
    return value[index] & 255;
  }

  public int queueOffsets() {
    return (writeOffset << 8) | readOffset;
  }

  public int queuedByteAt(final int index) {
    return pending[index] & 255;
  }

  private static int next(final int offset) {
    return offset + BYTES == QUEUE_BYTES ? 0 : offset + BYTES;
  }

  @Mutable
  @NoInline
  public void enqueue(final int low, final int high, final int multiplier) {
    for (int n = 0; n < multiplier; n++) {
      int carry = 0;
      int remaining = low;
      for (int i = 0; i < BYTES; i++) {
        if (i == 4) { remaining = high; }
        final int award = remaining & 255;
        remaining >>>= 8;
        final int previous = pending[writeOffset + i] & 255;
        int digit = (previous & 15) + (award & 15) + carry;
        carry = digit > 9 ? 1 : 0;
        if (digit > 9) {
          digit -= 10;
        }
        int tens = (previous >>> 4) + (award >>> 4) + carry;
        carry = tens > 9 ? 1 : 0;
        if (tens > 9) {
          tens -= 10;
        }
        pending[writeOffset + i] = (byte) ((tens << 4) | digit);
      }
      writeOffset = (byte) next(writeOffset);
    }
  }

  @Mutable
  @NoInline
  public void tick(final int frame) {
    if ((frame & 3) != 0) {
      return;
    }
    if (readOffset == writeOffset) {
      idleEnd = writeOffset;
    }
    final int low = RomMemory.readIntLE(pending,readOffset);
    final int high = (pending[readOffset + 4] & 255) | ((pending[readOffset + 5] & 255) << 8);
    if ((low | high) == 0) {
      readOffset = idleEnd;
      return;
    }
    add(low, high, 1);
    for (int i = 0; i < BYTES; i++) {
      pending[readOffset + i] = 0;
    }
    readOffset = (byte) next(readOffset);
  }

  @Mutable
  @NoInline
  public void add(final int low, final int high, final int multiplier) {
    for (int n = 0; n < multiplier; n++) {
      int carry = 0;
      int remaining = low;
      for (int i = 0; i < BYTES; i++) {
        if (i == 4) { remaining = high; }
        final int award = remaining & 255;
        remaining >>>= 8;
        final int previous = value[i] & 255;
        int ones = (previous & 15) + (award & 15) + carry;
        carry = ones > 9 ? 1 : 0;
        if (ones > 9) {
          ones -= 10;
        }
        int tens = (previous >>> 4) + (award >>> 4) + carry;
        carry = tens > 9 ? 1 : 0;
        if (tens > 9) {
          tens -= 10;
        }
        value[i] = (byte) ((tens << 4) | ones);
      }
      if (carry != 0) {
        for (int i = 0; i < BYTES; i++) {
          value[i] = (byte) 0x99;
        }
        break;
      }
    }
    version++;
  }
}

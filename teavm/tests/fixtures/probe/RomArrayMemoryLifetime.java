package probe;
import dev.java2gb.api.*;

public final class RomArrayMemoryLifetime {
  private RomArrayMemoryLifetime() {}
  static final class Box {
    final byte[] data;
    Box(final byte[] data) { this.data = data; }
  }
  @NoInline static Box box(final byte[] data) { return new Box(data); }
  @NoInline static int offset(final byte[] other) { return (other[0] & 255) - 9; }
  @Mutable public static void main(final String[] args) {
    final byte[] first = {1,2,3,4};
    final Box holder = box(first);
    final int value = RomMemory.readIntLE(holder.data, offset(new byte[]{9,8,7,6}));
    GameBoy.report(value);
  }
}

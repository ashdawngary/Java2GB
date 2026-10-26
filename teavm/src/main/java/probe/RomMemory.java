package probe;

import org.teavm.interop.Import;
import org.teavm.interop.c.Include;

@Include("rom_memory.h")
public final class RomMemory {
  private RomMemory() {}
  @NoInline
  public static int readIntLE(final byte[] source, final int offset) {
    return (source[offset] & 255) | ((source[offset + 1] & 255) << 8)
        | ((source[offset + 2] & 255) << 16) | ((source[offset + 3] & 255) << 24);
  }
  @Import(name = "gb_rom_array_vram") public static native void copyToVram(final byte[] source, final int offset,
      final int address, final int plane, final int length);
  @Import(name = "gb_rom_array_port") public static native void copyToPort(final byte[] source, final int offset,
      final int port, final int length);
  @Import(name = "gb_rom_array_oam") public static native void copyToOam(final byte[] source, final int offset);
}

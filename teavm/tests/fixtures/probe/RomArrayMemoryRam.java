package probe;
import dev.java2gb.api.*;

public final class RomArrayMemoryRam {
  private RomArrayMemoryRam() {}
  @Mutable public static void main(final String[] args) {
    GameBoy.init();
    final byte[] data = new byte[65];
    final byte[] lengths = {0, 1, 15, 16, 17, 31, 32, 33, 65};
    for (int i = 0; i < data.length; i++) { data[i] = (byte) (i + 1); }
    int failures = 0;
    for (int test = 0; test < lengths.length; test++) {
      final int length = lengths[test];
      GameBoyHardware.lcdOff();
      for (int i = 0; i < data.length + 2; i++) { GameBoyHardware.write(0x9800 + i, 0xa5); }
      GameBoyHardware.write(0xff40, 0x91);
      RomMemory.copyToVram(data, 0, 0x9801, 0, length);
      GameBoyHardware.lcdOff();
      for (int i = 0; i < data.length + 2; i++) {
        final int expected = i >= 1 && i <= length ? i : 0xa5;
        if (GameBoyHardware.read(0x9800 + i) != expected) { failures++; }
      }
    }
    GameBoy.report(failures == 0 ? RomMemory.readIntLE(data,0) : -failures);
  }
}

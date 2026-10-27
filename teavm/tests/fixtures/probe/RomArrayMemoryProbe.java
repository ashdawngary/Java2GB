package probe;
import dev.java2gb.api.*;

public final class RomArrayMemoryProbe {
  private RomArrayMemoryProbe() {}
  @RomData(bank=2) static final byte[] DATA = {1, -128, -1, 127, 5, 6, 7, 8};
  @RomData(bank=3) static final byte[] OAM = new byte[160];
  static int failures;
  private static void check(final boolean value) { if (!value) { failures++; } }
  @Mutable public static void main(final String[] args) {
    GameBoy.init();
    final byte[] ram = {9, 10, 11, -1, 13, 14, 15, 16};
    for (int i=0; i<100; i++) {
      final byte[] selected = (i&1)==0 ? DATA : ram;
      final int expected = (i&1)==0 ? 0x7fff8001 : 0xff0b0a09;
      check(RomArrayReaders.packed(selected,0)==expected);
      check(RomArrayReaders.packed(DATA,1)==0x057fff80);
      check(RomArrayReaders.packed(DATA,4)==0x08070605);
    }
    GameBoyHardware.write(0xff4f,1);
    RomArrayReaders.copy(DATA,0,0x9100,0,8);
    check((GameBoyHardware.read(0xff4f)&1)==1);
    RomArrayReaders.copy(ram,0,0x9200,1,8);
    RomArrayReaders.copy(DATA,8,0x9300,0,0);
    RomMemory.copyToOam(OAM,0);
    GameBoyHardware.write(0xff68,128);
    RomMemory.copyToPort(DATA,0,0xff69,8);
    check(DATA[0]==1 && DATA[7]==8 && ram[0]==9 && ram[7]==16);
    GameBoy.report(failures);
  }
}

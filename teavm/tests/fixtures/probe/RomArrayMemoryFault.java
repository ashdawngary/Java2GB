package probe;
import dev.java2gb.api.*;

public final class RomArrayMemoryFault {
  private RomArrayMemoryFault() {}
  @RomData(bank=2) static final byte[] DATA = {1,2,3,4,5,6,7,8};
  static int mode, index, length, destination, plane;
  public static void main(final String[] args) {
    GameBoy.init();
    while (GameBoy.buttons()==0) { }
    if (mode==0) {
      GameBoy.report(RomMemory.readIntLE(DATA,index));
    } else if (mode==1) {
      RomMemory.copyToVram(DATA,index,destination,plane,length);
    } else if (mode==2) {
      RomMemory.copyToOam(DATA,index);
    } else if (mode==3) {
      RomMemory.copyToPort(DATA,index,destination,length);
    } else {
      GameBoy.report(RomMemory.readIntLE(null,index));
    }
    GameBoy.report(-1);
  }
}

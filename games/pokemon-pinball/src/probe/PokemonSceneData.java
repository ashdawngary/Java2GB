package probe;
import dev.java2gb.api.*;

public final class PokemonSceneData {
  private PokemonSceneData() {}
  @RomData private static final byte[] OAM0 = {
    124,21,77,2,116,21,72,49,124,13,73,2,116,13,72,17,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,
    -16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,
    -16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,
    -16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,
    -16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,
  };
  @RomData private static final byte[] OAM1 = {
    120,70,113,64,104,78,114,96,112,78,114,96,120,78,112,96,120,65,113,64,120,57,113,64,120,49,113,64,120,41,113,64,
    120,33,113,64,104,18,114,64,112,18,114,64,120,26,113,64,120,18,112,64,100,18,114,64,100,78,114,96,84,78,114,96,
    92,78,114,96,76,78,114,32,84,18,114,64,92,18,114,64,76,18,114,0,36,70,113,0,68,18,114,0,68,78,114,32,
    60,78,114,32,52,78,114,32,44,78,114,32,36,65,113,0,36,57,113,0,36,49,113,0,36,78,112,32,36,41,113,0,
    36,33,113,0,60,18,114,0,52,18,114,0,44,18,114,0,36,26,113,0,36,18,112,0,-16,-16,-16,-16,-16,-16,-16,-16,
  };
  @RomData private static final byte[] OAM2 = {
    120,-114,116,64,104,-106,117,96,112,-106,117,96,120,-106,115,96,120,-119,116,64,120,-127,116,64,120,121,116,64,120,113,116,64,
    120,105,116,64,104,90,117,64,112,90,117,64,120,98,116,64,120,90,115,64,100,90,117,64,100,-106,117,96,84,-106,117,96,
    92,-106,117,96,76,-106,117,32,84,90,117,64,92,90,117,64,76,90,117,0,36,-114,116,0,68,90,117,0,68,-106,117,32,
    60,-106,117,32,52,-106,117,32,44,-106,117,32,36,-119,116,0,36,-127,116,0,36,121,116,0,36,-106,115,32,36,113,116,0,
    36,105,116,0,60,90,117,0,52,90,117,0,44,90,117,0,36,98,116,0,36,90,115,0,-16,-16,-16,-16,-16,-16,-16,-16,
  };
  @RomData private static final byte[] OAM3 = {
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
  };
  @RomData private static final byte[] OAM4 = {
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
  };
  @RomData private static final byte[] OAM5 = {
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
  };
  @RomData private static final byte[] OAM6 = {
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
  };
  @RomData private static final byte[] OAM7 = {
    80,64,83,17,80,56,82,17,72,64,81,17,88,44,80,17,80,44,79,17,124,21,77,2,116,21,72,49,124,13,73,2,
    116,13,72,17,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,
    -16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,
    -16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,
    -16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,-16,
  };
  @RomData private static final byte[] PALETTE7 = {
    -1,127,-65,3,31,0,0,0,-1,127,-128,105,31,0,0,0,-1,127,-9,99,31,0,0,0,-1,127,-65,3,-128,105,0,0,
    -1,127,-108,126,-128,105,0,0,-1,127,-9,99,-128,105,0,0,-1,127,-108,126,-9,99,0,0,-64,0,-64,0,-64,0,-64,0,
    -108,82,-1,127,-65,3,0,0,-1,127,-1,127,31,0,0,0,-108,82,-1,127,-9,110,0,0,-64,0,-64,0,-64,0,-64,0,
    -64,0,-64,0,-64,0,-64,0,-64,0,-64,0,-64,0,-64,0,-64,0,-64,0,-64,0,-64,0,-64,0,-64,0,-64,0,-64,0,
  };
  @RomData private static final byte[] OAM8 = {
    120,70,113,64,104,78,114,96,112,78,114,96,120,78,112,96,120,65,113,64,120,57,113,64,120,49,113,64,120,41,113,64,
    120,33,113,64,104,18,114,64,112,18,114,64,120,26,113,64,120,18,112,64,100,18,114,64,100,78,114,96,84,78,114,96,
    92,78,114,96,76,78,114,32,84,18,114,64,92,18,114,64,76,18,114,0,36,70,113,0,68,18,114,0,68,78,114,32,
    60,78,114,32,52,78,114,32,44,78,114,32,36,65,113,0,36,57,113,0,36,49,113,0,36,78,112,32,36,41,113,0,
    36,33,113,0,60,18,114,0,52,18,114,0,44,18,114,0,36,26,113,0,36,18,112,0,-16,-16,-16,-16,-16,-16,-16,-16,
  };
  @RomData private static final byte[] PALETTE8 = {
    -1,127,-97,2,31,0,0,0,-1,127,-64,126,0,124,0,0,-1,127,31,0,32,3,0,0,31,124,31,124,31,124,31,124,
    31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,
    74,41,-1,127,-75,86,0,0,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,
    -1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,
  };
  @RomData private static final byte[] OAM9 = {
    120,-114,116,64,104,-106,117,96,112,-106,117,96,120,-106,115,96,120,-119,116,64,120,-127,116,64,120,121,116,64,120,113,116,64,
    120,105,116,64,104,90,117,64,112,90,117,64,120,98,116,64,120,90,115,64,100,90,117,64,100,-106,117,96,84,-106,117,96,
    92,-106,117,96,76,-106,117,32,84,90,117,64,92,90,117,64,76,90,117,0,36,-114,116,0,68,90,117,0,68,-106,117,32,
    60,-106,117,32,52,-106,117,32,44,-106,117,32,36,-119,116,0,36,-127,116,0,36,121,116,0,36,-106,115,32,36,113,116,0,
    36,105,116,0,60,90,117,0,52,90,117,0,44,90,117,0,36,98,116,0,36,90,115,0,-16,-16,-16,-16,-16,-16,-16,-16,
  };
  @RomData private static final byte[] PALETTE9 = {
    -1,127,-97,2,31,0,0,0,-1,127,-64,126,0,124,0,0,-1,127,31,0,32,3,0,0,31,124,31,124,31,124,31,124,
    31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,31,124,
    74,41,-1,127,-75,86,0,0,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,
    -1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,-1,127,
  };
  @RomData private static final byte[] OAM10 = {
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
  };
  @RomData private static final byte[] PALETTE10 = {
    -1,127,-115,126,-97,16,0,0,-1,127,-8,3,31,0,3,0,-1,127,43,127,96,125,0,0,-1,127,-65,53,31,0,0,0,
    -1,127,31,124,31,0,0,0,-1,127,-108,82,8,33,0,0,-1,127,-108,82,8,33,0,0,-1,127,56,13,-128,100,0,0,
    -75,86,-1,127,-65,16,0,0,-1,127,-75,86,-69,2,0,0,-75,86,-1,127,-75,110,0,0,-1,127,-1,127,8,33,0,0,
    -75,86,-97,3,29,0,0,0,-1,127,-1,127,8,33,0,0,-108,82,-32,103,64,58,0,0,-1,127,-33,67,27,35,119,14,
  };
  @RomData private static final byte[] OAM11 = {
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
  };
  @RomData private static final byte[] PALETTE11 = {
    -1,127,-115,126,-97,16,0,0,-1,127,-8,3,31,0,3,0,-1,127,43,127,96,125,0,0,-1,127,-65,53,31,0,0,0,
    -1,127,31,124,31,0,0,0,-8,3,31,124,31,0,0,0,-1,127,-83,125,31,0,0,0,-1,127,-65,53,31,0,0,0,
    -75,86,-1,127,-65,16,0,0,-1,127,127,90,21,0,4,0,-108,82,-1,127,31,124,0,0,-1,127,-1,127,-33,2,-118,0,
    -108,82,-14,75,101,2,-32,0,-1,127,-97,2,-1,65,69,0,-108,82,-32,103,64,58,0,0,-1,127,-1,53,21,0,4,0,
  };
  @RomData private static final byte[] OAM12 = {
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
  };
  @RomData private static final byte[] PALETTE12 = {
    -1,127,-115,126,-97,16,0,0,-1,127,43,127,96,125,0,0,-1,127,-28,54,-96,17,0,0,-1,127,-65,3,15,1,0,0,
    -1,127,31,0,16,0,0,0,-1,127,-108,82,8,33,0,0,-1,127,-108,82,8,33,0,0,-1,127,56,13,-128,100,0,0,
    -75,86,-1,127,-65,16,0,0,-1,127,-75,86,-69,2,0,0,-75,86,-1,127,-75,110,0,0,-1,127,-1,127,8,33,0,0,
    -75,86,-97,3,29,0,0,0,-1,127,-1,127,8,33,0,0,-75,86,-32,103,64,58,0,0,-1,127,-33,67,27,35,119,14,
  };
  @RomData private static final byte[] OAM13 = {
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
  };
  @RomData private static final byte[] PALETTE13 = {
    -1,127,-115,126,-97,16,0,0,-1,127,43,127,96,125,0,0,-1,127,-28,54,-96,17,0,0,-1,127,-65,3,15,1,0,0,
    -1,127,31,0,16,0,0,0,-1,127,-108,82,8,33,0,0,-1,127,-83,125,31,0,0,0,-1,127,-108,82,8,33,0,0,
    -75,86,-1,127,-65,16,0,0,-1,127,-65,61,-105,24,0,0,-75,86,-1,127,95,3,-54,0,-1,127,-40,106,76,57,0,0,
    -75,86,-1,127,8,33,0,0,-1,127,-1,127,8,33,0,0,-75,86,-32,103,64,58,0,0,-1,127,-1,127,8,33,0,0,
  };
  public static void load(final int asset) {
    GameBoyHardware.lcdOff();
    GameBoyHardware.raster(255,0,0);
    switch (asset) {
      case 0:
        PokemonPlaneData.copy(0,0x8000,0);
        GameBoyHardware.write(0xff4f,0);
        GameBoyHardware.write(65346,0);
        GameBoyHardware.write(65347,0);
        GameBoyHardware.write(65351,228);
        GameBoyHardware.write(65352,210);
        GameBoyHardware.write(65353,225);
        GameBoyHardware.write(65354,143);
        GameBoyHardware.write(65355,166);
        RomMemory.copyToOam(OAM0,0);
        GameBoyHardware.write(0xff40,195);
        return;
      case 1:
        PokemonPlaneData.copy(1,0x8000,0);
        GameBoyHardware.write(0xff4f,0);
        GameBoyHardware.write(65346,0);
        GameBoyHardware.write(65347,0);
        GameBoyHardware.write(65351,228);
        GameBoyHardware.write(65352,210);
        GameBoyHardware.write(65353,210);
        GameBoyHardware.write(65354,143);
        GameBoyHardware.write(65355,166);
        RomMemory.copyToOam(OAM1,0);
        GameBoyHardware.write(0xff40,195);
        return;
      case 2:
        PokemonPlaneData.copy(1,0x8000,0);
        GameBoyHardware.write(0xff4f,0);
        GameBoyHardware.write(65346,0);
        GameBoyHardware.write(65347,0);
        GameBoyHardware.write(65351,228);
        GameBoyHardware.write(65352,210);
        GameBoyHardware.write(65353,210);
        GameBoyHardware.write(65354,143);
        GameBoyHardware.write(65355,166);
        RomMemory.copyToOam(OAM2,0);
        GameBoyHardware.write(0xff40,195);
        return;
      case 3:
        PokemonPlaneData.copy(2,0x8000,0);
        GameBoyHardware.write(0xff4f,0);
        GameBoyHardware.write(65346,0);
        GameBoyHardware.write(65347,32);
        GameBoyHardware.write(65351,228);
        GameBoyHardware.write(65352,225);
        GameBoyHardware.write(65353,228);
        GameBoyHardware.write(65354,144);
        GameBoyHardware.write(65355,7);
        RomMemory.copyToOam(OAM3,0);
        GameBoyHardware.write(0xff40,231);
        return;
      case 4:
        PokemonPlaneData.copy(3,0x8000,0);
        GameBoyHardware.write(0xff4f,0);
        GameBoyHardware.write(65346,0);
        GameBoyHardware.write(65347,32);
        GameBoyHardware.write(65351,228);
        GameBoyHardware.write(65352,225);
        GameBoyHardware.write(65353,228);
        GameBoyHardware.write(65354,134);
        GameBoyHardware.write(65355,7);
        RomMemory.copyToOam(OAM4,0);
        GameBoyHardware.write(0xff40,231);
        return;
      case 5:
        PokemonPlaneData.copy(4,0x8000,0);
        GameBoyHardware.write(0xff4f,0);
        GameBoyHardware.write(65346,0);
        GameBoyHardware.write(65347,32);
        GameBoyHardware.write(65351,228);
        GameBoyHardware.write(65352,225);
        GameBoyHardware.write(65353,228);
        GameBoyHardware.write(65354,144);
        GameBoyHardware.write(65355,7);
        RomMemory.copyToOam(OAM5,0);
        GameBoyHardware.write(0xff40,231);
        return;
      case 6:
        PokemonPlaneData.copy(5,0x8000,0);
        GameBoyHardware.write(0xff4f,0);
        GameBoyHardware.write(65346,0);
        GameBoyHardware.write(65347,32);
        GameBoyHardware.write(65351,228);
        GameBoyHardware.write(65352,225);
        GameBoyHardware.write(65353,228);
        GameBoyHardware.write(65354,134);
        GameBoyHardware.write(65355,7);
        RomMemory.copyToOam(OAM6,0);
        GameBoyHardware.write(0xff40,231);
        return;
      case 7:
        PokemonPlaneData.copy(6,0x8000,0);
        PokemonPlaneData.copy(7,0x8000,1);
        GameBoyHardware.write(0xff4f,0);
        GameBoyHardware.write(65384,128);
        RomMemory.copyToPort(PALETTE7,0,65385,64);
        GameBoyHardware.write(65386,128);
        RomMemory.copyToPort(PALETTE7,64,65387,64);
        GameBoyHardware.write(65346,0);
        GameBoyHardware.write(65347,0);
        GameBoyHardware.write(65351,0);
        GameBoyHardware.write(65352,0);
        GameBoyHardware.write(65353,0);
        GameBoyHardware.write(65354,143);
        GameBoyHardware.write(65355,166);
        RomMemory.copyToOam(OAM7,0);
        GameBoyHardware.write(0xff40,195);
        return;
      case 8:
        PokemonPlaneData.copy(1,0x8000,0);
        PokemonPlaneData.copy(8,0x8000,1);
        GameBoyHardware.write(0xff4f,0);
        GameBoyHardware.write(65384,128);
        RomMemory.copyToPort(PALETTE8,0,65385,64);
        GameBoyHardware.write(65386,128);
        RomMemory.copyToPort(PALETTE8,64,65387,64);
        GameBoyHardware.write(65346,0);
        GameBoyHardware.write(65347,0);
        GameBoyHardware.write(65351,0);
        GameBoyHardware.write(65352,0);
        GameBoyHardware.write(65353,0);
        GameBoyHardware.write(65354,143);
        GameBoyHardware.write(65355,166);
        RomMemory.copyToOam(OAM8,0);
        GameBoyHardware.write(0xff40,195);
        return;
      case 9:
        PokemonPlaneData.copy(1,0x8000,0);
        PokemonPlaneData.copy(8,0x8000,1);
        GameBoyHardware.write(0xff4f,0);
        GameBoyHardware.write(65384,128);
        RomMemory.copyToPort(PALETTE9,0,65385,64);
        GameBoyHardware.write(65386,128);
        RomMemory.copyToPort(PALETTE9,64,65387,64);
        GameBoyHardware.write(65346,0);
        GameBoyHardware.write(65347,0);
        GameBoyHardware.write(65351,0);
        GameBoyHardware.write(65352,0);
        GameBoyHardware.write(65353,0);
        GameBoyHardware.write(65354,143);
        GameBoyHardware.write(65355,166);
        RomMemory.copyToOam(OAM9,0);
        GameBoyHardware.write(0xff40,195);
        return;
      case 10:
        PokemonPlaneData.copy(9,0x8000,0);
        PokemonPlaneData.copy(10,0x8000,1);
        GameBoyHardware.write(0xff4f,0);
        GameBoyHardware.write(65384,128);
        RomMemory.copyToPort(PALETTE10,0,65385,64);
        GameBoyHardware.write(65386,128);
        RomMemory.copyToPort(PALETTE10,64,65387,64);
        GameBoyHardware.write(65346,0);
        GameBoyHardware.write(65347,32);
        GameBoyHardware.write(65351,0);
        GameBoyHardware.write(65352,0);
        GameBoyHardware.write(65353,0);
        GameBoyHardware.write(65354,144);
        GameBoyHardware.write(65355,7);
        RomMemory.copyToOam(OAM10,0);
        GameBoyHardware.write(0xff40,231);
        return;
      case 11:
        PokemonPlaneData.copy(11,0x8000,0);
        PokemonPlaneData.copy(12,0x8000,1);
        GameBoyHardware.write(0xff4f,0);
        GameBoyHardware.write(65384,128);
        RomMemory.copyToPort(PALETTE11,0,65385,64);
        GameBoyHardware.write(65386,128);
        RomMemory.copyToPort(PALETTE11,64,65387,64);
        GameBoyHardware.write(65346,0);
        GameBoyHardware.write(65347,32);
        GameBoyHardware.write(65351,228);
        GameBoyHardware.write(65352,225);
        GameBoyHardware.write(65353,228);
        GameBoyHardware.write(65354,134);
        GameBoyHardware.write(65355,7);
        RomMemory.copyToOam(OAM11,0);
        GameBoyHardware.write(0xff40,231);
        return;
      case 12:
        PokemonPlaneData.copy(13,0x8000,0);
        PokemonPlaneData.copy(14,0x8000,1);
        GameBoyHardware.write(0xff4f,0);
        GameBoyHardware.write(65384,128);
        RomMemory.copyToPort(PALETTE12,0,65385,64);
        GameBoyHardware.write(65386,128);
        RomMemory.copyToPort(PALETTE12,64,65387,64);
        GameBoyHardware.write(65346,0);
        GameBoyHardware.write(65347,32);
        GameBoyHardware.write(65351,0);
        GameBoyHardware.write(65352,0);
        GameBoyHardware.write(65353,0);
        GameBoyHardware.write(65354,144);
        GameBoyHardware.write(65355,7);
        RomMemory.copyToOam(OAM12,0);
        GameBoyHardware.write(0xff40,231);
        return;
      case 13:
        PokemonPlaneData.copy(15,0x8000,0);
        PokemonPlaneData.copy(16,0x8000,1);
        GameBoyHardware.write(0xff4f,0);
        GameBoyHardware.write(65384,128);
        RomMemory.copyToPort(PALETTE13,0,65385,64);
        GameBoyHardware.write(65386,128);
        RomMemory.copyToPort(PALETTE13,64,65387,64);
        GameBoyHardware.write(65346,0);
        GameBoyHardware.write(65347,32);
        GameBoyHardware.write(65351,228);
        GameBoyHardware.write(65352,225);
        GameBoyHardware.write(65353,228);
        GameBoyHardware.write(65354,134);
        GameBoyHardware.write(65355,7);
        RomMemory.copyToOam(OAM13,0);
        GameBoyHardware.write(0xff40,231);
        return;
      default: GameBoyHardware.trap(2); return;
    }
  }
}

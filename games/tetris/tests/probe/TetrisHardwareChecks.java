package probe;

public final class TetrisHardwareChecks {
  private TetrisHardwareChecks() {}
  public static int command, completed;

  @NoInline
  static int audioHash() {
    final int first = TetrisAssets.AUDIO_START, end = TetrisAssets.AUDIO_END;
    if (TetrisHardware.audioData(-1) != 0 || TetrisHardware.audioData(first - 1) != 0
        || TetrisHardware.audioData(end) != 0 || TetrisHardware.audioData(Integer.MAX_VALUE) != 0
        || TetrisHardware.audioWord(-1) != 0 || TetrisHardware.audioWord(first - 1) != 0
        || TetrisHardware.audioWord(end - 1) != 0 || TetrisHardware.audioWord(Integer.MAX_VALUE) != 0) { return 0; }
    int hash = 1;
    for (int address = first; address < end; address++) {
      hash = hash * 31 + TetrisHardware.audioData(address);
      if (address < end - 1) { hash = hash * 31 + TetrisHardware.audioWord(address); }
    }
    return hash;
  }

  @NoInline
  static void run(final int test) {
    TetrisHardware.lcd(TetrisHardware.LCD_OFF);
    switch (test) {
      case 1: TetrisHardware.tiles(0); break;
      case 2: TetrisHardware.tiles(1); break;
      case 3:
        GameBoyHardware.vramFill(0x9800,0xa5,2048);
        for (int length = 0; length <= TetrisHardware.PACKED_TILE_BYTES; length++) {
          TetrisHardware.backgroundPacked(16,length,length,0x78563412);
        }
        TetrisHardware.backgroundRun(0,31,20,0x9b);
        TetrisHardware.backgroundRun(0,30,0,0x9b);
        GameBoyHardware.vramSpan(0x9ffc,0x89abcdef,4);
        GameBoyHardware.vramSpan(0x9fff,0x12,1);
        GameBoyHardware.vramSpan(0x8000,0x12,0);
        GameBoyHardware.vramFill(0x8000,0x12,0);
        break;
      case 4:
        for (int slot = 0; slot < TetrisLayout.SPRITE_COUNT; slot++) {
          TetrisHardware.sprite(slot,slot * 17 - 270,slot * 23 - 300,slot * 31,slot * 7);
        }
        TetrisHardware.sprite(-1,0,0,0,0);
        TetrisHardware.sprite(TetrisLayout.SPRITE_COUNT,0,0,0,0);
        break;
      case 5:
        TetrisHardware.audio(TetrisHardware.NR52,TetrisHardware.AUDIO_ENABLED);
        TetrisHardware.audio(TetrisHardware.NR51,0x45);
        TetrisHardware.audio(TetrisHardware.NR10 - 1,0xff);
        TetrisHardware.audio(TetrisHardware.WAVE_RAM + TetrisHardware.WAVE_BYTES,0xff);
        break;
      case 6:
        GameBoy.report(audioHash());
        completed = test;
        TetrisHardware.lcd(TetrisHardware.LCD_ON);
        return;
      case 7: TetrisHardware.backgroundPacked(19,0,2,0); break;
      case 8: TetrisHardware.backgroundRun(-1,0,1,0); break;
      case 9: TetrisHardware.backgroundPacked(0,0,-1,0); break;
      case 10: GameBoyHardware.vramSpan(0x7fff,0,1); break;
      case 11: GameBoyHardware.vramSpan(0x8000,0,5); break;
      case 12: GameBoyHardware.vramSpan(0x9fff,0,2); break;
      case 13: GameBoyHardware.vramFill(0xa000,0,0); break;
      case 14: GameBoyHardware.vramFill(0x8000,0,-1); break;
      case 15: GameBoyHardware.vramFill(0x8000,0,8193); break;
      default: GameBoyHardware.trap(2); return;
    }
    TetrisHardware.lcd(TetrisHardware.LCD_ON);
    GameBoy.report(test);
    completed = test;
  }

  public static void main(final String[] args) {
    GameBoy.init();
    TetrisHardware.lcd(TetrisHardware.LCD_ON);
    GameBoy.report(0);
    while (true) {
      if (command != completed) { run(command); }
      GameBoy.waitFrame();
    }
  }
}

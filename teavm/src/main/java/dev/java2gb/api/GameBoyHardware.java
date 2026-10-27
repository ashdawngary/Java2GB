package dev.java2gb.api;

import org.teavm.interop.Import;
import org.teavm.interop.c.Include;

@Include("gb_hardware.h")
public final class GameBoyHardware {
  private GameBoyHardware() {}
  @Import(name = "gb_hw_color") public static native boolean color();
  @Import(name = "gb_hw_fast") public static native void fast();
  @Import(name = "gb_hw_clock") public static native int clock();
  @Import(name = "gb_hw_lcd_off") public static native void lcdOff();
  @Import(name = "gb_hw_read") public static native int read(final int address);
  @Import(name = "gb_hw_write") public static native void write(final int address, final int value);
  @Import(name = "gb_hw_oam") public static native void oamPacked(final int slot, final int packed);
  @Import(name = "gb_hw_vram4") public static native void vramPacked(final int address, final int packed);
  @Import(name = "gb_hw_vram_fill") public static native void vramFill(final int address, final int value, final int length);
  @Import(name = "gb_hw_vram_span") public static native void vramSpan(final int address, final int packed, final int length);
  @Import(name = "gb_hw_vblank") public static native void vblank(final int enabled);
  @Import(name = "gb_hw_raster") public static native void raster(final int line, final int before, final int after);
  @Import(name = "gb_hw_trap") public static native void trap(final int code);
}

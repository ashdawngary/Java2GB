package probe;

import org.teavm.interop.Import;
import org.teavm.interop.c.Include;

@Include("gb_serial.h")
public final class GameBoySerial {
  private GameBoySerial() {}
  public static final int MANUAL = 0, REARM = 1, CLEAR_REPLY = 2, DELAY_REARM = 4;
  @Import(name = "gb_serial_open") public static native void open();
  @Import(name = "gb_serial_close") public static native void close();
  @Import(name = "gb_serial_arm") public static native void arm(final int data, final int control);
  @Import(name = "gb_serial_reload") public static native void reload(final int data, final int flags);
  @Import(name = "gb_serial_schedule") public static native void schedule(final int data);
  @Import(name = "gb_serial_delay250") public static native void delay250();
  @Import(name = "gb_serial_read") public static native int read();
}

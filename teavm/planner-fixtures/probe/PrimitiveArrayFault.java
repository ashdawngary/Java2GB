package probe;

public final class PrimitiveArrayFault {
  public static int kind, index, scenario;

  @NoInline
  static void access() {
    if (kind == 0) {
      boolean[] values = scenario == 1 ? new boolean[0] : scenario == 2 ? null : new boolean[2];
      GameBoy.report(values[index] ? 1 : 0);
    } else if (kind == 1) {
      byte[] values = scenario == 1 ? new byte[0] : scenario == 2 ? null : new byte[2];
      GameBoy.report(values[index]);
    } else if (kind == 2) {
      short[] values = scenario == 1 ? new short[0] : scenario == 2 ? null : new short[2];
      GameBoy.report(values[index]);
    } else if (kind == 3) {
      char[] values = scenario == 1 ? new char[0] : scenario == 2 ? null : new char[2];
      GameBoy.report(values[index]);
    } else {
      int[] values = scenario == 1 ? new int[0] : scenario == 2 ? null : new int[2];
      GameBoy.report(values[index]);
    }
  }

  public static void main(String[] args) {
    while (GameBoy.buttons() == 0) {
      GameBoy.waitFrame();
    }
    access();
  }
}

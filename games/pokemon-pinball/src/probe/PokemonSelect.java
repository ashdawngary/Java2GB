package probe;

public final class PokemonSelect {
  private PokemonSelect() {}
  private static final int FRAME_DURATION = 8, PHASE_MASK = 3;
  private static byte phase, remaining = FRAME_DURATION, drawnFrame = -1;
  private static boolean drawnBlue;

  public static void enter() {
    phase = 0;
    remaining = FRAME_DURATION;
    drawnFrame = -1;
  }

  public static void tick(final boolean blue) {
    final int frame = phase == 1 ? 1 : phase == 3 ? 2 : 0;
    if (drawnFrame != frame || drawnBlue != blue) {
      PokemonSelectData.draw(blue,frame);
      drawnFrame = (byte) frame;
      drawnBlue = blue;
    }
    if (--remaining == 0) {
      phase = (byte) ((phase + 1) & PHASE_MASK);
      remaining = FRAME_DURATION;
    }
  }
}

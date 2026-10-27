package probe;
import dev.java2gb.api.*;

public final class PokemonSpinner {
  public static final int DRAG = 7, TURN = 0x1800, MAX_TURNS = 100, MAX_CHARGE = 15;
  public static final int CHARGED_DELAY = 100, CHARGE_SOUND_FIRST = 0x12, FULL_SOUND = 0x11;
  public short velocity;
  public char state;
  public byte turns, charge, chargeLock, effect;

  public void reset() {
    velocity = 0;
    state = 0;
    turns = 0;
    charge = 0;
    chargeLock = 0;
    effect = 0;
  }

  @NoInline
  public boolean tick() {
    effect = 0;
    if (velocity == 0) {
      return false;
    }
    final int slowed = velocity < 0 ? velocity + DRAG : velocity - DRAG;
    velocity = (short) ((velocity < 0 ? slowed > 0 : slowed < 0) ? 0 : slowed);
    final int next = (char) (state + velocity);
    final boolean backwards = (next & 0x8000) != 0;
    final boolean turn = backwards || next >= TURN;
    state = (char) (backwards ? next + TURN : turn ? next - TURN : next);
    if (!turn) {
      return false;
    }
    if ((turns & 255) != MAX_TURNS) {
      turns++;
    }
    final boolean full = charge == MAX_CHARGE;
    if (!full) {
      charge++;
    }
    if (chargeLock == 0) {
      effect = (byte) (charge == MAX_CHARGE ? FULL_SOUND : CHARGE_SOUND_FIRST + charge);
    }
    if (!full && charge == MAX_CHARGE) {
      chargeLock = CHARGED_DELAY;
    }
    return true;
  }
}

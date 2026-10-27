package probe;
import dev.java2gb.api.*;

public final class PokemonPikachu {
  public static final int IDLE = 0, SAVING = 1, MISSED = 2, SAVE_RUMBLE = 96;
  private static final int SAVE_LIMIT = 100, EXTRA_INTERVAL = 10;
  public static final int RELEASE_INDEX = 17, RELEASE_SPEED = -0x400, SAVE_SOUND = 0x10, MISS_SOUND = 0x3b;
  @RomData private static final byte[] DURATIONS = {12,5,5,5,5,5,6,6,6,6,6,6,6,6,6,6,6,1,0};
  @RomData private static final byte[] POSES = {2,3,2,4,5,2,6,7,8,2,5,8,7,2,8,7,2,0};
  public byte state, side, duration, index, pose, saves;
  public boolean rewardActive;

  public void reset() {
    state = IDLE;
    side = 0;
    duration = 0;
    index = 0;
    pose = 0;
    saves = 0;
    rewardActive = false;
  }

  @Mutable
  @NoInline
  public void tick(final PokemonBall ball, final PokemonRules rules, final int hitSide, final int buttons, final int frame) {
    if (hitSide >= 0 && state == IDLE && (rewardActive || hitSide == side)) {
      duration = DURATIONS[0];
      index = 0;
      pose = POSES[0];
      if (rewardActive || rules.spinner.charge == PokemonSpinner.MAX_CHARGE) {
        state = SAVING;
        if (!rewardActive) { rules.spinner.charge = 0; }
        ball.vx = 0;
        ball.vy = 0;
        ball.spin = 0;
        ball.rotation = 0;
        ball.gravity = false;
      } else {
        state = MISSED;
        rules.effect = MISS_SOUND;
      }
    }
    if (state == IDLE) {
      if ((buttons & GameBoy.LEFT) != 0) { side = 0; }
      else if ((buttons & GameBoy.A) != 0) { side = 1; }
      pose = (byte) ((frame >>> 4) & 1);
    } else if (duration != 0) {
      duration--;
      if (duration == 0) {
        index++;
        duration = state == MISSED ? (byte) (index == 1 ? 1 : 0) : DURATIONS[index];
        if (duration != 0) {
          pose = state == MISSED ? 0 : POSES[index];
        }
        if (state == MISSED && index == 1) {
          state = IDLE;
        } else if (state == SAVING && index == 1) {
          rules.rumbleActive = true;
          rules.rumbleTimer = SAVE_RUMBLE;
          if ((saves & 255) < SAVE_LIMIT) {
            saves++;
            if (saves % EXTRA_INTERVAL == 0) { rules.extraBall(); }
          }
          rules.effect = SAVE_SOUND;
        } else if (state == SAVING && index == RELEASE_INDEX) {
          ball.vy = RELEASE_SPEED;
          ball.gravity = true;
          rules.score.enqueue(PokemonScore.FIVE_THOUSAND,0,rules.ballType == PokemonRules.POKE_BALL ? 1 : rules.ballType);
          state = IDLE;
        }
      }
    }
  }

  public int drawSide(final PokemonBall ball, final int frame) {
    if (!rewardActive) { return side; }
    return state == IDLE ? (frame >>> 3) & 1 : (ball.x >>> 8) < PokemonConstants.FIELD_CENTER_X ? 0 : 1;
  }
}

package probe;

import static probe.PokemonObjectData.*;
import static probe.PokemonConstants.*;

public final class PokemonRules {
  public static final int POKE_BALL = 0, GREAT_BALL = 2, ULTRA_BALL = 3, MASTER_BALL = 5;
  public static final int UPGRADE_FRAMES = 3600, LIGHT_BLINK_FRAMES = 128;
  public static final int BUMPER_FORCE = 0x200, BUMPER_RUMBLE_FRAMES = 3;
  public static final int START_SAVER_FRAMES = 1200, MAX_EXTRA_BALLS = 10;
  private static final int RED_RAILING_FIRST = 33, BLUE_RAILING_FIRST = 15;
  private static final int RED_PIKACHU_FIRST = 28, BLUE_PIKACHU_FIRST = 13;
  public final PokemonScore score = new PokemonScore();
  public final PokemonSpinner spinner = new PokemonSpinner();
  public final PokemonBonusMultiplier bonus = new PokemonBonusMultiplier();
  public final PokemonPikachu pikachu = new PokemonPikachu();
  public byte extraBalls;
  public byte ballType, upgradeMask, caveMask, rumbleTimer, partyCount, upgradeBlink, caveBlink;
  public byte caveCompletions, slotOpenDelay, redStructureBackup = 4;
  public byte alleyFlags, leftAlleyCount, rightAlleyCount = 2;
  public byte indicator0 = (byte) 128, indicator1 = (byte) 130, indicator2, indicator3 = (byte) 128;
  public char upgradeTimer, saver = START_SAVER_FRAMES;
  public boolean rumbleActive, openedCave, capturing, timerGraphicsActive;
  public byte effect;

  @Mutable
  public void reset() {
    score.clear();
    spinner.reset();
    bonus.reset();
    pikachu.reset();
    extraBalls = 0;
    ballType = POKE_BALL;
    partyCount = 0;
    upgradeMask = 0;
    caveMask = 0;
    caveCompletions = 0;
    slotOpenDelay = 0;
    redStructureBackup = 4;
    openedCave = false;
    capturing = false;
    alleyFlags = 0;
    leftAlleyCount = 0;
    rightAlleyCount = 2;
    indicator0 = (byte) 128;
    indicator1 = (byte) 130;
    indicator2 = 0;
    indicator3 = (byte) 128;
    timerGraphicsActive = false;
    upgradeTimer = 0;
    upgradeBlink = 0;
    caveBlink = 0;
    saver = START_SAVER_FRAMES;
    rumbleActive = false;
    rumbleTimer = 0;
  }

  public void lostBall() {
    spinner.velocity = 0;
    spinner.charge = 0;
    spinner.chargeLock = 0;
    spinner.turns = 0;
    pikachu.rewardActive = false;
    pikachu.saves = 0;
    caveMask = 0;
    upgradeMask = 0;
    caveCompletions = 0;
    ballType = POKE_BALL;
    bonus.reset();
  }

  private int multiplier() {
    return ballType == POKE_BALL ? 1 : ballType;
  }

  public void readyBall(final PokemonBall ball, final boolean blue) {
    if (!blue && (redStructureBackup & 128) != 0) {
      redStructureBackup = (byte) (ball.collisionState & ~1);
    }
    timerGraphicsActive = false;
    ball.ready(blue);
  }

  @Mutable
  private void award(final int packed) {
    score.enqueue(packed, 0, multiplier());
  }

  @Mutable
  public void extraBall() {
    if (extraBalls < MAX_EXTRA_BALLS) {
      extraBalls++;
    } else {
      score.enqueue(PokemonScore.TEN_MILLION, 0, 1);
    }
  }

  private void bump(final PokemonBall ball, final boolean sling, final int id) {
    ball.flipperForce = BUMPER_FORCE;
    ball.flipperCollision = true;
    if (sling) {
      ball.normal += (id == 6 || id == 1) ? -8 : 8;
    }
    ball.bumperHits++;
    rumbleTimer = BUMPER_RUMBLE_FRAMES;
    rumbleActive = true;
    effect = (byte) (sling ? 0x0b : 0x0e);
  }

  @Mutable
  @NoInline
  public void tick(final PokemonBall ball, final int pressed, final int buttons, final int frame) {
    effect = 0;
    if (rumbleTimer != 0) {
      rumbleTimer--;
    } else {
      rumbleActive = false;
    }
    if (saver != 0 && (ball.x >>> FRACTION_BITS) < SCROLL_BALL_X) {
      saver--;
    }
    final int event = PokemonObjects.detect(ball);
    final int group = event >>> FRACTION_BITS, id = event & BYTE_MASK;
    if (group == RED_VOLTORB || group == BLUE_SHELLDER) {
      bump(ball, false, id);
      award(PokemonScore.FIVE_HUNDRED);
    } else if (group == RED_BUMPERS || group == BLUE_BUMPERS) {
      bump(ball, true, id);
    }
    if ((ball.stage & 1) != 0) {
      final int side = group == RED_MULTIPLIER ? id - RED_RAILING_FIRST : group == BLUE_MULTIPLIER ? id - BLUE_RAILING_FIRST : -1;
      bonus.tick(ball.stage == BLUE_BOTTOM, side, frame);
      if (side >= 0) {
        award(PokemonScore.TEN);
        effect = 0x0d;
        if (bonus.extraBall) { extraBall(); }
      }
    }
    if (group == RED_SPINNER || group == BLUE_SPINNER) {
      spinner.velocity = ball.vy;
    }
    if (spinner.tick()) {
      award(PokemonScore.TEN);
      if (spinner.effect != 0) {
        effect = spinner.effect;
      }
    }
    if (spinner.charge == PokemonSpinner.MAX_CHARGE && spinner.chargeLock != 0 && --spinner.chargeLock == 90) {
      effect = 0x22;
    }
    PokemonLights.tick(this, ball, event, pressed);
    if (group == RED_LAUNCH || group == BLUE_LAUNCH) {
      alleyFlags = 0;
      ball.launch();
      ball.previousObject = -1;
      effect = 0x0a;
    }
    final int saverSide = group == RED_PIKACHU ? id - RED_PIKACHU_FIRST : group == BLUE_PIKACHU ? id - BLUE_PIKACHU_FIRST : -1;
    pikachu.tick(ball, this, saverSide, buttons, frame);
    if (group == RED_BOARD || group == BLUE_BOARD) {
      PokemonBoard.hit(this, ball, id - (group == RED_BOARD ? 17 : 7));
    }
  }
}

package probe;

import static probe.PokemonConstants.*;

public final class PokemonBoard {
  private PokemonBoard() {}
  public static final int LEFT = 1, RIGHT = 2, STARYU = 4, MAX_ALLEYS = 3, BLINK = 128;

  public static void restore(final PokemonRules rules, final PokemonBall ball) {
    if ((rules.redStructureBackup & BLINK) == 0) {
      ball.collisionState = (byte) ((ball.collisionState & 1) | rules.redStructureBackup);
      rules.redStructureBackup = -1;
      rules.timerGraphicsActive = true;
    }
  }

  private static void left(final PokemonRules rules, final PokemonBall ball, final boolean blue) {
    if ((rules.alleyFlags & LEFT) == 0) {
      return;
    }
    rules.alleyFlags &= ~LEFT;
    if (rules.leftAlleyCount == MAX_ALLEYS) {
      return;
    }
    rules.leftAlleyCount++;
    if (blue && rules.leftAlleyCount == MAX_ALLEYS) {
      rules.indicator0 = MAX_ALLEYS;
      rules.indicator2 = (byte) BLINK;
    } else {
      rules.indicator0 = (byte) (rules.leftAlleyCount | BLINK);
      if (!blue && rules.leftAlleyCount == MAX_ALLEYS) {
        ball.collisionState = (byte) ((ball.collisionState & 1) | 6);
      }
    }
  }

  private static void right(final PokemonRules rules) {
    if ((rules.alleyFlags & RIGHT) == 0) {
      return;
    }
    rules.alleyFlags &= ~RIGHT;
    if (rules.rightAlleyCount == MAX_ALLEYS) {
      return;
    }
    rules.rightAlleyCount++;
    rules.indicator1 = (byte) (rules.rightAlleyCount == MAX_ALLEYS ? MAX_ALLEYS : rules.rightAlleyCount | BLINK);
    if (rules.rightAlleyCount >= 2) {
      rules.indicator3 = (byte) BLINK;
    }
  }

  @Mutable
  @NoInline
  public static void hit(final PokemonRules rules, final PokemonBall ball, final int lane) {
    final boolean blue = ball.stage == BLUE_TOP;
    rules.score.enqueue(PokemonScore.FIVE, 0, rules.ballType == PokemonRules.POKE_BALL ? 1 : rules.ballType);
    if (blue) {
      if (ball.collisionState == 0) {
        ball.collisionState = 1;
        rules.timerGraphicsActive = true;
      }
      if (lane == 0) {
        left(rules,ball,true);
      } else if (lane == 1) {
        right(rules);
      } else {
        rules.alleyFlags = (byte) (lane == 2 ? LEFT : RIGHT);
      }
      return;
    }
    if (lane == 0 || lane == 1) {
      left(rules,ball,false);
    } else if (lane == 2) {
      rules.alleyFlags &= ~STARYU;
    } else if (lane == 5 || lane == 7) {
      right(rules);
    } else {
      rules.alleyFlags = (byte) (lane == 3 ? LEFT : lane == 4 ? STARYU : RIGHT);
      restore(rules,ball);
    }
  }
}

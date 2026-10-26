package probe;

import static probe.PokemonConstants.*;
import static probe.PokemonObjectData.*;

public final class PokemonLights {
  private PokemonLights() {}
  public static final int CAVE_MASK = 15, UPGRADE_MASK = 7, SLOT_OPEN_DELAY = 3;
  public static final int COUNT_LIMIT = 100, LANE_SOUND = 9, UPGRADE_SOUND = 0x3a, MASTER_SOUND = 0x4d;

  private static int rotate(final int mask, final int width, final int pressed) {
    final int limit = (1 << width) - 1;
    if ((pressed & GameBoy.LEFT) != 0) {
      return ((mask >>> 1) | (mask << (width - 1))) & limit;
    }
    if ((pressed & GameBoy.A) != 0) {
      return ((mask << 1) | (mask >>> (width - 1))) & limit;
    }
    return mask;
  }

  public static void advanceCave(final PokemonRules rules, final int pressed) {
    if (rules.caveBlink == 0) {
      rules.caveMask = (byte) rotate(rules.caveMask, 4, pressed);
      return;
    }
    rules.caveBlink--;
    if (rules.caveBlink == 0) {
      rules.openedCave = true;
      rules.slotOpenDelay = SLOT_OPEN_DELAY;
    }
    if ((rules.caveBlink & 7) == 0) {
      rules.caveMask = (byte) ((rules.caveBlink & 8) == 0 ? 0 : CAVE_MASK);
    }
  }

  public static void advanceUpgrade(final PokemonRules rules, final int pressed) {
    if (rules.upgradeBlink == 0) {
      rules.upgradeMask = (byte) rotate(rules.upgradeMask, 3, pressed);
      return;
    }
    rules.upgradeBlink--;
    if ((rules.upgradeBlink & 7) == 0) {
      rules.upgradeMask = (byte) ((rules.upgradeBlink & 8) == 0 ? 0 : UPGRADE_MASK);
    }
  }

  private static int multiplier(final PokemonRules rules) {
    return rules.ballType == PokemonRules.POKE_BALL ? 1 : rules.ballType;
  }

  @Mutable
  public static void cave(final PokemonRules rules, final int lane, final int pressed) {
    if (lane < 0 || rules.caveBlink != 0) {
      advanceCave(rules, pressed);
      return;
    }
    final int bit = 1 << lane;
    if ((rules.caveMask & bit) != 0) {
      return;
    }
    rules.caveMask |= bit;
    rules.score.enqueue(PokemonScore.HUNDRED, 0, multiplier(rules));
    if (rules.caveMask == CAVE_MASK) {
      rules.caveBlink = (byte) PokemonRules.LIGHT_BLINK_FRAMES;
      rules.score.enqueue(PokemonScore.FOUR_HUNDRED, 0, multiplier(rules));
      rules.effect = LANE_SOUND;
      if ((rules.caveCompletions & BYTE_MASK) != COUNT_LIMIT) {
        rules.caveCompletions++;
      }
    }
  }

  @Mutable
  public static void upgrade(final PokemonRules rules, final PokemonBall ball, final int lane, final int pressed) {
    final boolean blue = ball.stage == BLUE_TOP;
    if (blue && lane >= 0 && ball.collisionState == 0) {
      ball.collisionState = 1;
      rules.timerGraphicsActive = true;
    }
    if (lane < 0 || (ball.collisionState & 1) == 0 || rules.upgradeBlink != 0) {
      advanceUpgrade(rules, pressed);
      return;
    }
    rules.alleyFlags = 0;
    if (!blue) {
      PokemonBoard.restore(rules,ball);
    }
    final int bit = 1 << lane;
    if (blue) {
      rules.upgradeMask ^= bit;
    } else {
      if ((rules.upgradeMask & bit) != 0) {
        return;
      }
      rules.upgradeMask |= bit;
    }
    final int multiplier = multiplier(rules);
    rules.score.enqueue(PokemonScore.HUNDRED, 0, multiplier);
    if (rules.upgradeMask != UPGRADE_MASK) {
      rules.effect = LANE_SOUND;
      return;
    }
    rules.upgradeBlink = (byte) PokemonRules.LIGHT_BLINK_FRAMES;
    rules.upgradeTimer = PokemonRules.UPGRADE_FRAMES;
    rules.score.enqueue(PokemonScore.FOUR_HUNDRED, 0, multiplier);
    if (rules.ballType == PokemonRules.MASTER_BALL) {
      rules.effect = MASTER_SOUND;
      rules.score.enqueue(PokemonScore.MILLION, 0, 1);
    } else {
      rules.effect = UPGRADE_SOUND;
      rules.ballType = (byte) (rules.ballType == PokemonRules.POKE_BALL ? PokemonRules.GREAT_BALL
          : rules.ballType == PokemonRules.GREAT_BALL ? PokemonRules.ULTRA_BALL : PokemonRules.MASTER_BALL);
    }
  }

  public static void decay(final PokemonRules rules) {
    if (rules.capturing || rules.upgradeTimer == 0 || --rules.upgradeTimer != 0) {
      return;
    }
    rules.ballType = (byte) (rules.ballType == PokemonRules.GREAT_BALL ? PokemonRules.POKE_BALL
        : rules.ballType == PokemonRules.ULTRA_BALL ? PokemonRules.GREAT_BALL
        : rules.ballType == PokemonRules.MASTER_BALL ? PokemonRules.ULTRA_BALL : PokemonRules.POKE_BALL);
    if (rules.ballType != PokemonRules.POKE_BALL) {
      rules.upgradeTimer = PokemonRules.UPGRADE_FRAMES;
    }
  }

  @Mutable
  @NoInline
  public static void tick(final PokemonRules rules, final PokemonBall ball, final int event, final int pressed) {
    final int group = event >>> FRACTION_BITS, id = event & BYTE_MASK;
    final int upgradeLane = group == RED_UPGRADE ? id - 14 : group == BLUE_UPGRADE ? id - 19 : -1;
    final int caveLane = group == RED_CAVE ? id - 10 : group == BLUE_CAVE ? id - 22 : -1;
    if ((ball.stage & 1) == 0) {
      upgrade(rules, ball, upgradeLane, pressed);
    } else {
      advanceUpgrade(rules, pressed);
    }
    decay(rules);
    cave(rules, caveLane, pressed);
  }
}

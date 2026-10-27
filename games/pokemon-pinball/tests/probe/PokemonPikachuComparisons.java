package probe;
import dev.java2gb.api.*;

public final class PokemonPikachuComparisons {
  private PokemonPikachuComparisons() {}
  @Mutable
  @NoInline
  public static int run() {
    final PokemonBall ball = new PokemonBall();
    final PokemonRules rules = new PokemonRules();
    final PokemonPikachu saver = rules.pikachu;
    for (int i = 0; i < PokemonPikachuCases.COUNT; i++) {
      rules.score.clear();
      ball.stage = (byte) (PokemonPikachuCases.c0(i) == 0 ? 1 : 5);
      saver.rewardActive = PokemonPikachuCases.c4(i) != 0;
      saver.state = (byte) PokemonPikachuCases.c5(i);
      saver.side = (byte) PokemonPikachuCases.c6(i);
      saver.duration = (byte) PokemonPikachuCases.c7(i);
      saver.index = (byte) PokemonPikachuCases.c8(i);
      saver.pose = (byte) PokemonPikachuCases.c9(i);
      saver.saves = (byte) PokemonPikachuCases.c10(i);
      rules.spinner.charge = (byte) PokemonPikachuCases.c11(i);
      rules.spinner.chargeLock = (byte) PokemonPikachuCases.c12(i);
      rules.rumbleTimer = (byte) PokemonPikachuCases.c13(i);
      ball.vx = (short) PokemonPikachuCases.c14(i);
      ball.vy = (short) PokemonPikachuCases.c15(i);
      ball.spin = (byte) PokemonPikachuCases.c16(i);
      ball.rotation = (byte) PokemonPikachuCases.c17(i);
      ball.gravity = PokemonPikachuCases.c18(i) != 0;
      rules.extraBalls = (byte) PokemonPikachuCases.c19(i);
      saver.tick(ball,rules,(short)PokemonPikachuCases.c1(i),PokemonPikachuCases.c2(i),PokemonPikachuCases.c3(i));
      if ((saver.state & 255) != PokemonPikachuCases.c20(i)
          || (saver.side & 255) != PokemonPikachuCases.c21(i)
          || (saver.duration & 255) != PokemonPikachuCases.c22(i)
          || (saver.index & 255) != PokemonPikachuCases.c23(i)
          || (saver.pose & 255) != PokemonPikachuCases.c24(i)
          || (saver.saves & 255) != PokemonPikachuCases.c25(i)
          || (rules.spinner.charge & 255) != PokemonPikachuCases.c26(i)
          || (rules.spinner.chargeLock & 255) != PokemonPikachuCases.c27(i)
          || (rules.rumbleTimer & 255) != PokemonPikachuCases.c28(i)
          || (ball.vx & 65535) != PokemonPikachuCases.c29(i)
          || (ball.vy & 65535) != PokemonPikachuCases.c30(i)
          || (ball.spin & 255) != PokemonPikachuCases.c31(i)
          || (ball.rotation & 255) != PokemonPikachuCases.c32(i)
          || (ball.gravity ? 1 : 0) != PokemonPikachuCases.c33(i)
          || (rules.extraBalls & 255) != PokemonPikachuCases.c34(i)) { return -i - 1; }
      int digest = 1;
      for (int offset = 0; offset < PokemonScore.QUEUE_BYTES; offset++) {
        digest = digest * 31 + rules.score.queuedByteAt(offset);
      }
      if ((digest & 65535) != PokemonPikachuCases.c35(i) || (digest >>> 16) != PokemonPikachuCases.c36(i)
          || (rules.score.queueOffsets() >>> 8) != PokemonPikachuCases.c37(i)) { return -i - 1; }
    }
    return PokemonPikachuCases.COUNT;
  }
}

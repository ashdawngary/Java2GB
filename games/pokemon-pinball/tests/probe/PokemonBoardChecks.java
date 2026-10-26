package probe;

public final class PokemonBoardChecks {
  private PokemonBoardChecks() {}

  @Mutable
  public static int run() {
    final PokemonRules rules = new PokemonRules();
    final PokemonBall ball = new PokemonBall();
    for (int i = 0; i < PokemonBoardCases.COUNT; i++) {
      rules.reset();
      ball.stage = (byte) PokemonBoardCases.c0(i);
      rules.alleyFlags = (byte) PokemonBoardCases.c1(i);
      rules.leftAlleyCount = (byte) PokemonBoardCases.c2(i);
      rules.rightAlleyCount = (byte) PokemonBoardCases.c3(i);
      ball.collisionState = (byte) PokemonBoardCases.c5(i);
      rules.redStructureBackup = (byte) PokemonBoardCases.c6(i);
      rules.ballType = (byte) PokemonBoardCases.c7(i);
      PokemonBoard.hit(rules,ball,PokemonBoardCases.c4(i));
      int hash = 1;
      for (int j = 0; j < PokemonScore.QUEUE_BYTES; j++) {
        hash = hash * 31 + rules.score.queuedByteAt(j);
      }
      if (rules.alleyFlags != PokemonBoardCases.c8(i) || rules.leftAlleyCount != PokemonBoardCases.c9(i)
          || rules.rightAlleyCount != PokemonBoardCases.c10(i) || ball.collisionState != PokemonBoardCases.c11(i)
          || (rules.redStructureBackup & 255) != PokemonBoardCases.c12(i)
          || (rules.indicator0 & 255) != PokemonBoardCases.c13(i) || (rules.indicator1 & 255) != PokemonBoardCases.c14(i)
          || (rules.indicator2 & 255) != PokemonBoardCases.c15(i) || (rules.indicator3 & 255) != PokemonBoardCases.c16(i)
          || rules.timerGraphicsActive != (PokemonBoardCases.c17(i) != 0)
          || (rules.score.queueOffsets() >>> 8) != PokemonBoardCases.c18(i)
          || hash != (PokemonBoardCases.c19(i) | (PokemonBoardCases.c20(i) << 16))) {
        return -i - 1;
      }
    }
    return PokemonBoardCases.COUNT;
  }

  public static void main(final String[] args) {
    GameBoy.report(run());
  }
}

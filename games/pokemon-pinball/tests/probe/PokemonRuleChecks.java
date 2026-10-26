package probe;

public final class PokemonRuleChecks {
  private PokemonRuleChecks() {}

  public static int objects() {
    final PokemonBall ball = new PokemonBall();
    for (int i = 0; i < PokemonObjectCases.COUNT; i++) {
      ball.stage = (byte) PokemonObjectCases.c0(i);
      ball.x = (char) ((PokemonObjectCases.c1(i) << 8) | 91);
      ball.y = (char) ((PokemonObjectCases.c2(i) << 8) | 179);
      ball.attribute = (byte) PokemonObjectCases.c3(i);
      ball.colliding = PokemonObjectCases.c4(i) != 0;
      ball.previousObject = (byte) PokemonObjectCases.c5(i);
      ball.disabledObjects = PokemonObjectCases.c6(i) | (PokemonObjectCases.c7(i) << 16);
      final int event = PokemonObjects.detect(ball) & 65535;
      if (event != PokemonObjectCases.c8(i) || (ball.previousObject & 255) != PokemonObjectCases.c9(i)) {
        return -i - 1;
      }
    }
    return PokemonObjectCases.COUNT;
  }

  public static int scores() {
    final PokemonScore score = new PokemonScore();
    for (int i = 0; i < PokemonScoreCases.COUNT; i++) {
      score.clear();
      score.add(PokemonScoreCases.c0(i) | (PokemonScoreCases.c1(i) << 8)
          | (PokemonScoreCases.c2(i) << 16) | (PokemonScoreCases.c3(i) << 24),
          PokemonScoreCases.c4(i) | (PokemonScoreCases.c5(i) << 8), 1);
      score.add(PokemonScoreCases.c6(i) | (PokemonScoreCases.c7(i) << 8)
          | (PokemonScoreCases.c8(i) << 16) | (PokemonScoreCases.c9(i) << 24),
          PokemonScoreCases.c10(i) | (PokemonScoreCases.c11(i) << 8), PokemonScoreCases.c12(i));
      if (score.byteAt(0) != PokemonScoreCases.c13(i) || score.byteAt(1) != PokemonScoreCases.c14(i)
          || score.byteAt(2) != PokemonScoreCases.c15(i) || score.byteAt(3) != PokemonScoreCases.c16(i)
          || score.byteAt(4) != PokemonScoreCases.c17(i) || score.byteAt(5) != PokemonScoreCases.c18(i)) {
        return -PokemonObjectCases.COUNT - i - 1;
      }
    }
    return PokemonScoreCases.COUNT;
  }

  public static int hud() {
    final PokemonScore score = new PokemonScore();
    final PokemonHud hud = new PokemonHud();
    for (int i = 0; i < PokemonHudCases.COUNT; i++) {
      score.clear();
      score.add(PokemonScoreCases.c13(i) | (PokemonScoreCases.c14(i) << 8)
          | (PokemonScoreCases.c15(i) << 16) | (PokemonScoreCases.c16(i) << 24),
          PokemonScoreCases.c17(i) | (PokemonScoreCases.c18(i) << 8), 1);
      hud.format(score);
      for (int j = 0; j < 26; j++) {
        if (hud.tileAt(j) != PokemonHudCases.tile(i, j)) {
          return -PokemonObjectCases.COUNT - PokemonScoreCases.COUNT - i - 1;
        }
      }
    }
    for (int i = 0; i < PokemonWindowCases.COUNT; i++) {
      if (PokemonHud.nextWindowY(PokemonWindowCases.c0(i), PokemonWindowCases.c1(i), PokemonWindowCases.c2(i))
          != PokemonWindowCases.c3(i)) {
        return -PokemonObjectCases.COUNT - PokemonScoreCases.COUNT - PokemonHudCases.COUNT - i - 1;
      }
    }
    return PokemonHudCases.COUNT + PokemonWindowCases.COUNT;
  }

  public static int queue() {
    final PokemonScore score = new PokemonScore();
    score.clear();
    for (int i = 0; i < PokemonScoreTimeline.COUNT; i++) {
      if (PokemonScoreTimeline.c7(i) != 0) {
        score.enqueue(PokemonScoreTimeline.c1(i) | (PokemonScoreTimeline.c2(i) << 8)
            | (PokemonScoreTimeline.c3(i) << 16) | (PokemonScoreTimeline.c4(i) << 24),
            PokemonScoreTimeline.c5(i) | (PokemonScoreTimeline.c6(i) << 8), PokemonScoreTimeline.c7(i));
      }
      score.tick(PokemonScoreTimeline.c0(i));
      if (score.byteAt(0) != PokemonScoreTimeline.c8(i) || score.byteAt(1) != PokemonScoreTimeline.c9(i)
          || score.byteAt(2) != PokemonScoreTimeline.c10(i) || score.byteAt(3) != PokemonScoreTimeline.c11(i)
          || score.byteAt(4) != PokemonScoreTimeline.c12(i) || score.byteAt(5) != PokemonScoreTimeline.c13(i)
          || score.queueOffsets() != PokemonScoreTimeline.c14(i)) {
        return -10000 - i;
      }
    }
    return PokemonScoreTimeline.COUNT;
  }

  public static int run() {
    final int objects = objects();
    if (objects < 0) {
      return objects;
    }
    final int scores = scores();
    if (scores < 0) {
      return scores;
    }
    final int queue = queue();
    if (queue < 0) {
      return queue;
    }
    final int hud = hud();
    return hud < 0 ? hud : objects + scores + queue + hud;
  }

  public static void main(final String[] args) {
    System.out.println(run());
  }
}

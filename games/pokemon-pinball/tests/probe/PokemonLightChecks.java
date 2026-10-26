package probe;

public final class PokemonLightChecks {
  private PokemonLightChecks() {}

  public static int motion() {
    final PokemonRules rules = new PokemonRules();
    for (int i = 0; i < PokemonLightMotionCases.COUNT; i++) {
      rules.caveMask = (byte) PokemonLightMotionCases.c1(i);
      rules.upgradeMask = rules.caveMask;
      rules.caveBlink = (byte) PokemonLightMotionCases.c2(i);
      rules.upgradeBlink = rules.caveBlink;
      rules.openedCave = false;
      rules.slotOpenDelay = 0;
      final boolean cave = PokemonLightMotionCases.c0(i) == 0;
      if (cave) {
        PokemonLights.advanceCave(rules, PokemonLightMotionCases.c3(i));
      } else {
        PokemonLights.advanceUpgrade(rules, PokemonLightMotionCases.c3(i));
      }
      if ((cave ? rules.caveMask : rules.upgradeMask) != PokemonLightMotionCases.c4(i)
          || ((cave ? rules.caveBlink : rules.upgradeBlink) & 255) != PokemonLightMotionCases.c5(i)
          || rules.openedCave != (PokemonLightMotionCases.c6(i) != 0)
          || rules.slotOpenDelay != PokemonLightMotionCases.c7(i)) {
        return -i - 1;
      }
    }
    return PokemonLightMotionCases.COUNT;
  }

  @Mutable
  public static int hits() {
    final PokemonRules rules = new PokemonRules();
    final PokemonBall ball = new PokemonBall();
    for (int i = 0; i < PokemonLightHitCases.COUNT; i++) {
      rules.reset();
      ball.stage = (byte) PokemonLightHitCases.c1(i);
      rules.caveMask = (byte) PokemonLightHitCases.c2(i);
      rules.upgradeMask = rules.caveMask;
      rules.caveBlink = (byte) PokemonLightHitCases.c3(i);
      rules.upgradeBlink = rules.caveBlink;
      ball.collisionState = (byte) PokemonLightHitCases.c6(i);
      rules.ballType = (byte) PokemonLightHitCases.c7(i);
      rules.redStructureBackup = (byte) PokemonLightHitCases.c8(i);
      rules.caveCompletions = (byte) PokemonLightHitCases.c9(i);
      final boolean cave = PokemonLightHitCases.c0(i) == 0;
      if (cave) {
        PokemonLights.cave(rules, PokemonLightHitCases.c5(i), PokemonLightHitCases.c4(i));
      } else {
        PokemonLights.upgrade(rules, ball, PokemonLightHitCases.c5(i), PokemonLightHitCases.c4(i));
      }
      int hash = 1;
      for (int j = 0; j < PokemonScore.QUEUE_BYTES; j++) {
        hash = hash * 31 + rules.score.queuedByteAt(j);
      }
      final int expectedHash = PokemonLightHitCases.c19(i) | (PokemonLightHitCases.c20(i) << 16);
      if ((cave ? rules.caveMask : rules.upgradeMask) != PokemonLightHitCases.c10(i)
          || ((cave ? rules.caveBlink : rules.upgradeBlink) & 255) != PokemonLightHitCases.c11(i)
          || ball.collisionState != PokemonLightHitCases.c12(i) || rules.ballType != PokemonLightHitCases.c13(i)
          || rules.upgradeTimer != PokemonLightHitCases.c14(i)
          || (rules.redStructureBackup & 255) != PokemonLightHitCases.c15(i)
          || rules.caveCompletions != PokemonLightHitCases.c16(i)
          || rules.openedCave != (PokemonLightHitCases.c17(i) != 0) || rules.slotOpenDelay != PokemonLightHitCases.c18(i)
          || hash != expectedHash || (rules.score.queueOffsets() >>> 8) != PokemonLightHitCases.c21(i)) {
        return -i - 1;
      }
    }
    return PokemonLightHitCases.COUNT;
  }

  public static int decay() {
    final PokemonRules rules = new PokemonRules();
    for (int i = 0; i < PokemonLightDecayCases.COUNT; i++) {
      rules.ballType = (byte) PokemonLightDecayCases.c0(i);
      rules.upgradeTimer = (char) PokemonLightDecayCases.c1(i);
      rules.capturing = PokemonLightDecayCases.c2(i) != 0;
      PokemonLights.decay(rules);
      if (rules.ballType != PokemonLightDecayCases.c3(i) || rules.upgradeTimer != PokemonLightDecayCases.c4(i)) {
        return -i - 1;
      }
    }
    return PokemonLightDecayCases.COUNT;
  }

  public static void main(final String[] args) {
    System.out.println(motion());
    System.out.println(hits());
    System.out.println(decay());
  }
}

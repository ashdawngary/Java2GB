package probe;

public final class PokemonLaunchChecks {
  private PokemonLaunchChecks() {}
  public static int run() {
    final PokemonRules rules = new PokemonRules();
    final PokemonBall ball = new PokemonBall();
    for (int i = 0; i < PokemonLaunchCases.COUNT; i++) {
      ball.collisionState = (byte) PokemonLaunchCases.c1(i);
      rules.redStructureBackup = (byte) PokemonLaunchCases.c2(i);
      rules.timerGraphicsActive = true;
      ball.vx = 1234;
      ball.vy = -2345;
      ball.spin = 17;
      ball.rotation = 33;
      ball.left = 0xfff;
      ball.right = 0x666;
      rules.readyBall(ball,PokemonLaunchCases.c0(i) != 0);
      if (ball.collisionState != PokemonLaunchCases.c3(i) || (rules.redStructureBackup & 255) != PokemonLaunchCases.c4(i)
          || ball.x != PokemonLaunchCases.c5(i) || ball.y != PokemonLaunchCases.c6(i)
          || ball.vx != PokemonLaunchCases.c7(i) || ball.vy != PokemonLaunchCases.c8(i)
          || ball.spin != PokemonLaunchCases.c9(i) || ball.rotation != PokemonLaunchCases.c10(i)
          || ball.left != PokemonLaunchCases.c11(i) || ball.right != PokemonLaunchCases.c12(i)
          || ball.gravity != (PokemonLaunchCases.c13(i) != 0) || rules.timerGraphicsActive != (PokemonLaunchCases.c14(i) != 0)) {
        return -i - 1;
      }
    }
    return PokemonLaunchCases.COUNT;
  }
  public static void main(final String[] args) {
    GameBoy.report(run());
  }
}

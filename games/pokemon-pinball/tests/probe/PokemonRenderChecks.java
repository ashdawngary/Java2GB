package probe;
import dev.java2gb.api.*;

public final class PokemonRenderChecks {
  private PokemonRenderChecks() {}
  static byte command, completed = -1, stage, camera, state, color, flipper, spinner, saverSide, saverPose;

  public static void main(final String[] args) {
    GameBoy.init();
    PokemonHardware.init();
    final PokemonBall ball = new PokemonBall();
    ball.x = 80 << 8;
    ball.y = 100 << 8;
    while (true) {
      final byte request = command;
      if (completed != request) {
        ball.stage = stage;
        ball.collisionState = state;
        ball.left = (char) (flipper << 8);
        ball.right = (char) (flipper << 8);
        PokemonRender.saver(saverSide,saverPose);
        PokemonRender.draw(ball, camera, spinner << 10, color != 0);
        completed = request;
      }
    }
  }
}

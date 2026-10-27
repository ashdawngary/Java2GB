package probe;
import dev.java2gb.api.*;

public final class PokemonCollisionChecks {
  private PokemonCollisionChecks() {}
  public static void main(final String[] args) {
    final PokemonBall ball = new PokemonBall();
    for (int i = 0; i < PokemonCases.COUNT; i++) {
      ball.stage = (byte) PokemonCases.stage(i);
      ball.collisionState = (byte) PokemonCases.state(i);
      ball.x = (char) PokemonCases.x(i);
      ball.y = (char) PokemonCases.y(i);
      ball.left = (char) PokemonCases.left(i);
      ball.right = (char) PokemonCases.right(i);
      ball.colliding = false;
      PokemonPhysics.surface(ball);
      final boolean hit = PokemonCases.hit(i) != 0;
      if (ball.colliding != hit || ball.x != PokemonCases.expected_x(i) || ball.y != PokemonCases.expected_y(i)
          || (hit && ((ball.normal & 255) != PokemonCases.normal(i) || (ball.attribute & 255) != PokemonCases.attribute(i)))) {
        GameBoy.report(-i - 1);
        return;
      }
    }
    GameBoy.report(PokemonCases.COUNT);
  }
}

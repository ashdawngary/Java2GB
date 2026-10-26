package probe;

import java.io.DataInputStream;
import java.io.FileInputStream;

public final class PokemonCollisionOriginalChecks {
  private PokemonCollisionOriginalChecks() {}
  public static void main(final String[] args) throws Exception {
    final PokemonBall ball = new PokemonBall();
    int compared = 0;
    try (final DataInputStream input = new DataInputStream(new FileInputStream(args[0]))) {
      final int count = input.readInt();
      for (int i = 0; i < count; i++) {
        ball.stage = (byte) input.readInt();
        ball.collisionState = (byte) input.readInt();
        ball.x = (char) input.readInt();
        ball.y = (char) input.readInt();
        final boolean hit = input.readInt() != 0;
        final int x = input.readInt(), y = input.readInt(), normal = input.readInt(), attribute = input.readInt();
        ball.left = (char) input.readInt();
        ball.right = (char) input.readInt();
        ball.colliding = false;
        PokemonPhysics.surface(ball);
        if (ball.colliding != hit || ball.x != x || ball.y != y ||
            (hit && ((ball.normal & 255) != normal || (ball.attribute & 255) != attribute))) {
          throw new AssertionError("Collision " + i + " stage/state=" + ball.stage + "/" + ball.collisionState
              + " expected=" + hit + "," + x + "," + y + "," + normal + "," + attribute
              + " actual=" + ball.colliding + "," + (int) ball.x + "," + (int) ball.y + ","
              + (ball.normal & 255) + "," + (ball.attribute & 255));
        }
        compared++;
      }
    }
    System.out.println(compared);
  }
}

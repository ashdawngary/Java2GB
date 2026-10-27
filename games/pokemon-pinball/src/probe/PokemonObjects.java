package probe;
import dev.java2gb.api.*;

import static probe.PokemonObjectData.*;
import static probe.PokemonConstants.*;

public final class PokemonObjects {
  private PokemonObjects() {}

  @NoInline
  public static int detect(final PokemonBall ball) {
    final int event = first(ball);
    if (event >= 0) {
      final int id = event & BYTE_MASK, previous = ball.previousObject & BYTE_MASK;
      ball.previousObject = (byte) id;
      return previous == id ? -1 : event;
    }
    ball.previousObject = -1;
    return -1;
  }
}

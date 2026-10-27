package probe;
import dev.java2gb.api.*;

public final class PokemonLightGraphicsChecks {
  private PokemonLightGraphicsChecks() {}
  static byte command = (byte) 128, completed = 127;
  public static void main(final String[] args) {
    GameBoy.init();
    while (true) {
      final int request = command & 255;
      final int next = request & 127;
      if ((completed & 255) != request) {
        if (next < 64) {
          PokemonLightData.cave(next >>> 4, next & 15);
        } else {
          PokemonLightData.upgrade((next - 64) >>> 3, next & 7);
        }
        completed = (byte) request;
      }
    }
  }
}

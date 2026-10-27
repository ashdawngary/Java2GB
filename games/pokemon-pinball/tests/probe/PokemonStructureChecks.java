package probe;
import dev.java2gb.api.*;

public final class PokemonStructureChecks {
  private PokemonStructureChecks() {}
  static byte command = (byte) 128, completed = 127, rendered;
  public static void main(final String[] args) {
    GameBoy.init();
    while (true) {
      final int request = command & 255;
      if ((completed & 255) != request) {
        final int code = request & 127;
        final int previous = (code >>> 3) & 7, current = code & 7;
        final boolean changed = PokemonStructureData.paint((code & 64) != 0,previous,current);
        rendered = (byte) (changed ? current : previous);
        completed = (byte) request;
      }
    }
  }
}

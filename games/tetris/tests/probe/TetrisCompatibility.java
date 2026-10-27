package probe;
import dev.java2gb.api.*;

public final class TetrisCompatibility {
  public static TetrisRules game;
  public static byte ready, command;
  public static char request, completed;
  public static int buttons, result;

  @NoInline
  static void execute() {
    if (command == 0) {
      result = game.fits(game.piece, game.x, game.y) ? 1 : 0;
    } else if (command == 1) {
      result = TetrisRules.speed(game.level) & 255;
    } else if (command == 2) {
      game.findLines();
      if (game.cleared != 0) {
        game.collapse();
      }
    } else if (command == 3) {
      game.lock();
    } else if (command == 4) {
      game.tick(buttons);
    }
  }

  public static void main(final String[] args) {
    GameBoy.init();
    game = new TetrisRules();
    ready = 1;
    while (true) {
      GameBoy.waitFrame();
      if (request != completed) {
        execute();
        completed = request;
      }
    }
  }
}

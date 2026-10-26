package probe;

import static probe.PokemonConstants.*;
import static probe.PokemonPinballGame.*;

public final class PokemonGameFlow {
  private PokemonGameFlow() {}
  @NoInline
  static void load(final int asset) {
    PokemonSceneData.load(asset + (color ? SCENES_PER_MODEL : 0));
  }

  @NoInline
  static void field() {
    load(ball.stage == RED_BOTTOM ? RED_BOTTOM_ASSET : ball.stage == RED_TOP ? RED_TOP_ASSET
        : ball.stage == BLUE_BOTTOM ? BLUE_BOTTOM_ASSET : BLUE_TOP_ASSET);
    PokemonHardware.raster(STATUS_SPLIT_LINE, FIELD_LCD, STATUS_LCD);
    hud.invalidate(ball.stage);
    PokemonLightVideo.invalidate();
    PokemonBonusVideo.invalidate();
    PokemonStructureVideo.invalidate();
    PokemonRender.invalidate();
  }

  @Mutable
  @NoInline
  static void newGame() {
    lives = INITIAL_BALLS;
    rules.reset();
    cameraX = SCROLL_MAX;
    ball.collisionState = (byte) (blue ? 0 : 4);
    rules.readyBall(ball, blue);
    scene = GAME_SCENE;
    paused = false;
    field();
  }

  @NoInline
  static void transition() {
    final int y = ball.y >>> FRACTION_BITS;
    if (y < UPPER_EXIT_Y || y >= 240) {
      if ((ball.stage & 1) != 0) {
        ball.stage--;
        ball.y = (char) (ball.y + (VIEW_CHANGE_Y << FRACTION_BITS));
        field();
        return;
      }
    } else if (y < LOWER_EXIT_Y) {
      return;
    } else if ((ball.stage & 1) == 0) {
      ball.stage++;
      ball.y = (char) (ball.y - (VIEW_CHANGE_Y << FRACTION_BITS));
      field();
      return;
    }
    if (rules.saver != 0) {
      rules.readyBall(ball, blue);
      ball.launch();
      cameraX = SCROLL_MAX;
      field();
      return;
    }
    if (rules.extraBalls != 0) {
      rules.extraBalls--;
    } else {
      lives--;
    }
    if (lives == 0) {
      scene = OVER_SCENE;
      PokemonHardware.rumble(false);
      return;
    }
    rules.lostBall();
    rules.readyBall(ball, blue);
    rules.saver = PokemonRules.START_SAVER_FRAMES;
    cameraX = SCROLL_MAX;
    field();
  }

  @NoInline
  static void tick(final int buttons) {
    final int pressed = buttons & ~(previous & BYTE_MASK);
    previous = (byte) buttons;
    frames++;
    if (scene == TITLE_SCENE) {
      if ((pressed & (GameBoy.START | GameBoy.A)) != 0) {
        scene = FIELD_SELECT_SCENE;
        PokemonGameFlow.load(blue ? SELECT_BLUE_ASSET : SELECT_RED_ASSET);
        PokemonSelect.enter();
      }
      return;
    }
    if (scene == FIELD_SELECT_SCENE) {
      if ((pressed & (GameBoy.LEFT | GameBoy.RIGHT)) != 0) {
        blue = (pressed & GameBoy.LEFT) == 0;
      }
      PokemonSelect.tick(blue);
      if ((pressed & GameBoy.B) != 0) {
        scene = TITLE_SCENE;
        PokemonGameFlow.load(TITLE_ASSET);
      } else if ((pressed & (GameBoy.START | GameBoy.A)) != 0) {
        PokemonGameFlow.newGame();
      }
      return;
    }
    if (scene == OVER_SCENE) {
      if ((pressed & (GameBoy.START | GameBoy.A)) != 0) {
        scene = FIELD_SELECT_SCENE;
        PokemonGameFlow.load(blue ? SELECT_BLUE_ASSET : SELECT_RED_ASSET);
        PokemonSelect.enter();
      }
      return;
    }
    if ((pressed & GameBoy.START) != 0) {
      paused = !paused;
      if (paused) {
        PokemonHardware.rumble(false);
      }
    }
    if (paused) {
      return;
    }
    if (!ball.launched) {
      if ((pressed & GameBoy.A) != 0) {
        ball.launch();
      }
    } else {
      PokemonPhysics.begin(ball, buttons);
      rules.tick(ball, pressed, buttons, PokemonHardware.clock());
      PokemonPhysics.finish(ball);
      PokemonHardware.rumble(rules.rumbleActive);
      PokemonGameFlow.transition();
    }
    final int camera = cameraX + ((ball.x >>> FRACTION_BITS) >= SCROLL_BALL_X ? SCROLL_STEP : -SCROLL_STEP);
    if (camera >= 0 && camera <= SCROLL_MAX) {
      cameraX = (byte) camera;
    }
    PokemonRender.saver(rules.pikachu.drawSide(ball,PokemonHardware.clock()),rules.pikachu.pose);
    PokemonRender.draw(ball, cameraX, rules.spinner.state, color);
    PokemonStructureVideo.draw(ball, color);
    PokemonLightVideo.draw(ball, rules, color);
    PokemonBonusVideo.draw(ball, rules, color);
    rules.score.tick(frames);
    hud.draw(ball, rules, lives);
  }

}

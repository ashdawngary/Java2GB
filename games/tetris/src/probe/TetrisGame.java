package probe;
import dev.java2gb.api.*;

import static probe.TetrisMenus.*;
import static probe.TetrisLayout.*;

public final class TetrisGame {
  public static final int SCENE_TITLE = 0, SCENE_CONFIG = 1, SCENE_DIFFICULTY = 2;
  public static final int SCENE_PLAY = 3, SCENE_LINK = 4;
  private static final int RESET_CHORD = GameBoy.START | GameBoy.SELECT | GameBoy.A | GameBoy.B;
  private static final int MAX_CATCHUP_UPDATES = 3, MAX_DRAW_NOTE_EVENTS = 3;
  private static final int GAME_OVER_WIPE_UPDATES = FIELD_ROWS * 2 + 1;
  private static final int GAME_OVER_LABEL_DELAY = 40;

  @RomData static final byte[] A_TYPE = RomText.ascii("A-TYPE");
  @RomData static final byte[] B_TYPE = RomText.ascii("B-TYPE");
  @RomData static final byte[] C_TYPE = RomText.ascii("C-TYPE");
  @RomData static final byte[] OFF_LABEL = RomText.ascii(" OFF  ");
  @RomData private static final byte[] CLEAR_LABEL = RomText.ascii("  CLEAR  ");
  @RomData private static final byte[] OVER_LABEL = RomText.ascii("GAME OVER");
  @RomData private static final byte[] START_LABEL = RomText.ascii("  START ");
  public static final TetrisRules game;
  public static final TetrisRender video;
  public static final TetrisSound sound;
  public static final TetrisMultiplayer link;
  public static byte scene, previous, selectedLevel, selectedHeight, mode, music, selection;
  public static byte players;
  public static boolean paused, preview = true;
  public static char lastClock;
  public static int frames, missedFrames, topA, topB;

  static {
    GameBoy.init();
    game = new TetrisRules();
    video = new TetrisRender();
    sound = new TetrisSound();
    link = new TetrisMultiplayer();
  }

  @NoInline
  public static void tick(final int buttons) {
    tick(buttons, true);
  }

  @NoInline
  public static void tick(final int buttons, final boolean present) {
    final int pressed = buttons & ~(previous & BYTE_MASK);
    previous = (byte) buttons;
    frames++;
    if ((buttons & RESET_CHORD) == RESET_CHORD) {
      link.title();
      players = 0;
      scene = SCENE_TITLE;
      video.screen(TITLE_SCREEN);
      video.show();
      sound.play(TetrisSound.SONG_TITLE);
      return;
    }
    if (scene == SCENE_LINK) {
      link.tick(buttons, pressed, present);
      return;
    }
    if (scene != SCENE_PLAY) {
      menus(buttons, pressed);
      sound.tick();
      return;
    }
    if (game.phase == TetrisRules.OVER || game.phase == TetrisRules.WON) {
      game.timer++;
      if (game.timer == 1) {
        if (mode == MODE_A && game.score > topA) {
          topA = game.score;
        }
        if (mode != MODE_A && game.score > topB) {
          topB = game.score;
        }
        sound.play(game.phase == TetrisRules.OVER ? TetrisSound.SONG_OVER : TetrisSound.SONG_CLEAR);
      }
      if (game.timer < GAME_OVER_WIPE_UPDATES && game.phase == TetrisRules.OVER && (game.timer & 1) == 0) {
        final int row = FIELD_ROWS - (game.timer >>> 1);
        for (int col = 0; col < FIELD_COLUMNS; col++) {
          GameBoy.background(col + FIELD_TILE_X, row, GAME_OVER_TILE);
        }
      }
      if (game.timer == GAME_OVER_LABEL_DELAY) {
        video.text(3, 6, game.phase == TetrisRules.OVER ? OVER_LABEL : CLEAR_LABEL);
        video.text(3, 8, START_LABEL);
      }
      if (game.timer > GAME_OVER_LABEL_DELAY && (pressed & (GameBoy.START | GameBoy.A | GameBoy.B)) != 0) {
        difficulty();
      }
      sound.tick();
      return;
    }
    if ((pressed & GameBoy.START) != 0) {
      paused = !paused;
      if (paused) {
        video.screen(mode == MODE_A ? PAUSE_A_SCREEN : PAUSE_B_SCREEN);
        video.hud(game);
        video.show();
        sound.play(TetrisSound.SONG_SILENT);
        sound.effect(TetrisSound.EFFECT_ROTATE);
      } else {
        video.screen(mode == MODE_A ? GAME_A_SCREEN : GAME_B_SCREEN);
        for (int row = 0; row < FIELD_ROWS; row++) {
          video.row(game, row, false);
        }
        video.game(game, preview);
        video.show();
        sound.play(song());
      }
    }
    if (paused) {
      sound.tick();
      return;
    }
    if ((pressed & GameBoy.SELECT) != 0) {
      preview = !preview;
    }
    advance(buttons, present);
  }

  @NoInline
  static void advance(final int buttons, final boolean present) {
    if (game.garbageRedraw || video.garbageWipe) {
      game.sound = TetrisSound.EFFECT_NONE;
      game.previous = (byte) buttons;
      if (game.timer > 1) { game.timer--; }
      video.wipeGarbage(game);
      sound.tick(true);
      return;
    }
    final int oldX = game.x, oldY = game.y, oldPiece = game.piece, oldPhase = game.phase;
    final boolean preparingGarbage = game.mode == MODE_LINK && game.phase == TetrisRules.ENTRY
        && game.lines != 0 && (game.garbagePending > 0 || game.garbageCursor >= 0);
    final int firstGarbageRow = game.garbageCursor < 0 ? 0 : game.garbageCursor;
    if (game.phase == TetrisRules.ENTRY
        && ((game.timer <= 1 && (video.redrawRow >= 0 || game.garbageRedraw))
            || (preparingGarbage && (video.redrawRow >= 0 || game.garbageRedraw
                || video.lastPhase != TetrisRules.ENTRY)))) {
      game.sound = TetrisSound.EFFECT_NONE;
    } else {
      game.tick(buttons);
      if (preparingGarbage) {
        video.prepareGarbage(game, firstGarbageRow,
            game.garbageCursor < 0 ? FIELD_ROWS : game.garbageCursor);
      }
    }
    sound.effect(game.sound);
    final boolean changed =
        oldX != game.x || oldY != game.y || oldPiece != game.piece || oldPhase != game.phase;
    sound.tick(changed || video.pendingRows() || game.phase == TetrisRules.SETTLE);
    if (present && sound.events < MAX_DRAW_NOTE_EVENTS) {
      video.game(game, preview);
    }
  }

  public static void main(final String[] args) {
    video.screen(TITLE_SCREEN);
    video.show();
    sound.play(TetrisSound.SONG_TITLE);
    link.title();
    lastClock = (char) TetrisHardware.clock();
    int pendingFrames = 0;
    while (true) {
      GameBoy.waitFrame();
      final int now = TetrisHardware.clock(), delta = (now - lastClock) & TetrisHardware.CLOCK_MASK;
      if (delta > 1) {
        missedFrames += delta - 1;
      }
      lastClock = (char) now;
      final int beforeScene = scene;
      final boolean beforePause = paused;
      int steps = 1;
      if ((scene == SCENE_PLAY || (scene == SCENE_LINK && link.stage == TetrisProtocol.PLAY)) && !paused && game.phase < TetrisRules.OVER) {
        pendingFrames += delta;
        steps = pendingFrames;
      } else {
        pendingFrames = 0;
      }
      if (steps > MAX_CATCHUP_UPDATES) {
        steps = MAX_CATCHUP_UPDATES;
      }
      if (pendingFrames > 0) {
        pendingFrames -= steps;
      }
      final int buttons = GameBoy.buttons();
      while (steps > 0) {
        tick(buttons, steps == 1);
        steps--;
      }
      if (beforeScene != scene || beforePause != paused) {
        lastClock = (char) TetrisHardware.clock();
        pendingFrames = 0;
      }
    }
  }
}

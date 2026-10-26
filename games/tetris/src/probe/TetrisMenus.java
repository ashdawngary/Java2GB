package probe;

import static probe.TetrisGame.*;
import static probe.TetrisLayout.*;

public final class TetrisMenus {
  static int song() {
    return TetrisSound.selectionSong(music);
  }

  @NoInline
  static void config() {
    scene = SCENE_CONFIG;
    selection = 0;
    video.screen(CONFIG_SCREEN);
    video.show();
    sound.play(song());
  }

  @NoInline
  static void difficulty() {
    scene = SCENE_DIFFICULTY;
    selection = 0;
    video.screen(mode == MODE_A ? DIFFICULTY_A_SCREEN : DIFFICULTY_B_SCREEN);
    if ((mode == MODE_A ? topA : topB) > 0) {
      video.number(12, 13, 6, mode == MODE_A ? topA : topB);
    }
    video.show();
  }

  @NoInline
  static void start() {
    game.reset(mode, selectedLevel, selectedHeight, TetrisHardware.entropy());
    video.screen(mode == MODE_A ? GAME_A_SCREEN : GAME_B_SCREEN);
    if (mode != MODE_A) {
      video.number(HEIGHT_X, HEIGHT_Y, 1, selectedHeight);
      for (int row = 0; row < FIELD_ROWS; row++) {
        video.row(game, row, false);
      }
    }
    paused = false;
    video.game(game, preview);
    video.show();
    sound.play(song());
    scene = SCENE_PLAY;
  }

  @NoInline
  static void digit(final int slot, final int value, final int x, final int y, final boolean selected) {
    TetrisHardware.sprite(slot, x, (!selected || ((frames >>> MENU_BLINK_SHIFT) & 1) == 0) ? y : HIDDEN_SPRITE_Y, value, 0);
  }

  static void musicLabel(final int first, final int choice, final boolean selected) {
    video.label(first, choice == 0 ? A_TYPE : choice == 1 ? B_TYPE : choice == 2 ? C_TYPE : OFF_LABEL,
        MUSIC_LABEL_X + (choice & 1) * MUSIC_COLUMN_SPACING,
        MUSIC_LABEL_Y + (choice >>> 1) * MUSIC_ROW_SPACING, selected, frames);
  }

  static void gridDigit(final int slot, final int value, final int columns,
      final int x, final int y, final boolean selected) {
    digit(slot, value, x + (value % columns) * DIGIT_SPACING,
        y + (value / columns) * DIGIT_SPACING, selected);
  }

  @NoInline
  static void menus(final int buttons, final int pressed) {
    if (scene == SCENE_TITLE) {
      if (link.titleTick()) {
        return;
      }
      if ((pressed & GameBoy.SELECT) != 0) {
        players ^= 1;
      }
      if ((pressed & GameBoy.RIGHT) != 0) {
        players = 1;
      } else if ((pressed & GameBoy.LEFT) != 0) {
        players = 0;
      }
      TetrisHardware.sprite(0, TITLE_CURSOR_X + players * TITLE_CURSOR_SPACING, TITLE_CURSOR_Y, TITLE_CURSOR_TILE, 0);
      if ((pressed & (players == 0 ? GameBoy.START | GameBoy.A : GameBoy.START)) != 0) {
        if (players == 0) {
          config();
        } else {
          link.lobby();
        }
      }
      return;
    }
    if (scene == SCENE_CONFIG) {
      if ((pressed & GameBoy.B) != 0) {
        if (selection == 1) {
          selection = 0;
        } else {
          scene = SCENE_TITLE;
          video.screen(TITLE_SCREEN);
          video.show();
          sound.play(TetrisSound.SONG_TITLE);
          return;
        }
      }
      if ((pressed & GameBoy.START) != 0) {
        difficulty();
        return;
      }
      if ((pressed & GameBoy.SELECT) != 0) {
        selection ^= 1;
      }
      if (selection == 1 && (pressed & GameBoy.DOWN) != 0 && music < 2) {
        music += 2;
        sound.play(song());
      }
      if (selection == 1 && (pressed & GameBoy.UP) != 0 && music >= 2) {
        music -= 2;
        sound.play(song());
      }
      if ((pressed & (GameBoy.LEFT | GameBoy.RIGHT)) != 0) {
        final int delta = (pressed & GameBoy.RIGHT) != 0 ? 1 : -1;
        if (selection == 0) {
          mode = (byte) (delta > 0 ? 1 : 0);
        } else {
          if (delta > 0 && (music & 1) == 0) {
            music++;
          }
          if (delta < 0 && (music & 1) != 0) {
            music--;
          }
          sound.play(song());
        }
        sound.effect(TetrisSound.EFFECT_MOVE);
      }
      if ((pressed & GameBoy.A) != 0) {
        if (selection == 0) {
          selection = 1;
        } else {
          difficulty();
        }
      }
      video.label(0, mode == MODE_A ? A_TYPE : B_TYPE, GAME_TYPE_LABEL_X + mode * GAME_TYPE_LABEL_SPACING, GAME_TYPE_LABEL_Y, selection == 0, frames);
      musicLabel(6, music, selection == 1);
    } else {
      if ((pressed & GameBoy.B) != 0) {
        config();
        return;
      }
      if ((pressed & GameBoy.START) != 0) {
        start();
        return;
      }
      if ((pressed & GameBoy.A) != 0) {
        if (mode != MODE_A && selection == 0) {
          selection = 1;
        } else {
          start();
          return;
        }
      }
      if (mode != MODE_A && (pressed & GameBoy.SELECT) != 0) {
        selection ^= 1;
      }
      int value = selection == 0 ? selectedLevel : selectedHeight;
      final int max = selection == 0 ? 9 : MAX_HEIGHT, stride = selection == 0 ? LEVEL_GRID_COLUMNS : HEIGHT_GRID_COLUMNS;
      if ((pressed & GameBoy.RIGHT) != 0 && value < max) {
        value++;
      }
      if ((pressed & GameBoy.LEFT) != 0 && value > 0) {
        value--;
      }
      if ((pressed & GameBoy.DOWN) != 0 && value + stride <= max) {
        value += stride;
      }
      if ((pressed & GameBoy.UP) != 0 && value >= stride) {
        value -= stride;
      }
      if (selection == 0) {
        selectedLevel = (byte) value;
      } else {
        selectedHeight = (byte) value;
      }
      final int lx = mode == MODE_A ? LEVEL_A_CURSOR_X : LEVEL_B_CURSOR_X;
      gridDigit(0, selectedLevel, LEVEL_GRID_COLUMNS, lx, DIFFICULTY_CURSOR_Y, selection == 0);
      if (mode != MODE_A) {
        gridDigit(1, selectedHeight, HEIGHT_GRID_COLUMNS, HEIGHT_B_CURSOR_X, DIFFICULTY_CURSOR_Y, selection == 1);
      }
    }
  }

}

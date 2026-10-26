package probe;

public final class TetrisLayout {
  private TetrisLayout() {}

  public static final int BYTE_MASK = 0xff;
  public static final int FIELD_COLUMNS = 10, FIELD_ROWS = 18, HIDDEN_ROWS = 2;
  public static final int VISIBLE_CELLS = FIELD_COLUMNS * FIELD_ROWS;
  public static final int BOARD_CELLS = FIELD_COLUMNS * (FIELD_ROWS + HIDDEN_ROWS);
  public static final int SCREEN_COLUMNS = 20, SCREEN_ROWS = 18;
  public static final int SCREEN_CELLS = SCREEN_COLUMNS * SCREEN_ROWS;
  public static final int TILE_PIXELS = 8, SPRITE_COUNT = 40, HIDDEN_SPRITE_Y = 160;
  public static final int FIELD_TILE_X = 2, FIELD_PIXEL_X = FIELD_TILE_X * TILE_PIXELS;
  public static final int GARBAGE_TEMPLATE_ROWS = 10;
  public static final int GARBAGE_CELLS = FIELD_COLUMNS * GARBAGE_TEMPLATE_ROWS;
  public static final int HEIGHT_ROWS = 2, MAX_HEIGHT = GARBAGE_TEMPLATE_ROWS / HEIGHT_ROWS;
  public static final int HEIGHT_GRID_COLUMNS = 3, LEVEL_GRID_COLUMNS = 5;
  public static final int DIGIT_SPACING = 16, MENU_BLINK_SHIFT = 4;
  public static final int MODE_A = 0, MODE_B = 1, MODE_LINK = 2;

  public static final int TITLE_SCREEN = 0, CONFIG_SCREEN = 1;
  public static final int DIFFICULTY_A_SCREEN = 2, DIFFICULTY_B_SCREEN = 3;
  public static final int GAME_A_SCREEN = 4, GAME_B_SCREEN = 5;
  public static final int PAUSE_A_SCREEN = 6, PAUSE_B_SCREEN = 7;
  public static final int LINK_HEIGHT_SCREEN = 8, LINK_GAME_SCREEN = 9;
  public static final int LINK_PAUSE_SCREEN = 10, RESULT_SCREEN = 11, SCREEN_COUNT = 12;

  public static final int EMPTY_TILE = 0x2f, LETTER_A_TILE = 0x0a, HYPHEN_TILE = 0x25;
  public static final int FIRST_BLOCK_TILE = 0x80, BLOCK_VARIANT_MASK = 7;
  public static final int GARBAGE_TILE = 0x28, FLASH_TILE = 0x8c;
  public static final int GAME_OVER_TILE = 0x87, HUD_FILL_TILE = 0x8e;
  public static final int GAUGE_TILE = 0xb6, TITLE_CURSOR_TILE = 0x58;
  public static final int MARIO_FACE_TILE = 0xae, LUIGI_FACE_TILE = 0xc0;
  public static final int SPRITE_BEHIND_BACKGROUND = 0x80, SPRITE_MIRROR_X = 0x20;
  public static final int PIECE_SLOT = 0, PREVIEW_SLOT = 4;
  public static final int PREVIEW_X = 120, PREVIEW_Y = 112;
  public static final int FACE_SPRITES = 4, MENU_FACE_SLOT = 24, GAME_FACE_SLOT = 32;
  public static final int GAUGE_SLOT = 12, GAUGE_X = 8;
  public static final int MENU_FACE_X = 32, MARIO_FACE_Y = 48, LUIGI_FACE_Y = 104;
  public static final int GAME_FACE_X = 124, GAME_FACE_Y = 8;
  public static final int HEIGHT_DIGIT_X = 88;
  public static final int SCORE_X = 13, SCORE_Y = 3, SCORE_DIGITS = 6;
  public static final int LINES_Y = 10, SINGLE_LINES_X = 14, SINGLE_LINES_DIGITS = 4;
  public static final int LINK_LINES_X = 16, LINK_LINES_DIGITS = 2;
  public static final int LEVEL_X = 16, LEVEL_A_Y = 7, LEVEL_B_Y = 2;
  public static final int LEVEL_A_DIGITS = 2, LEVEL_B_DIGITS = 1;
  public static final int HEIGHT_X = 16, HEIGHT_Y = 5;
  public static final int RESULT_TEXT_X = 5, RESULT_TEXT_Y = 6, RESULT_PROMPT_Y = 9;
  public static final int RESULT_WINS_Y = 12, RESULT_OUR_WINS_X = 6, RESULT_THEIR_WINS_X = 12;
  public static final int TITLE_CURSOR_X = 8, TITLE_CURSOR_Y = 112, TITLE_CURSOR_SPACING = 80;
  public static final int LEVEL_A_CURSOR_X = 40, LEVEL_B_CURSOR_X = 16, DIFFICULTY_CURSOR_Y = 48;
  public static final int HEIGHT_B_CURSOR_X = 104;
  public static final int GAME_TYPE_LABEL_X = 24, GAME_TYPE_LABEL_Y = 40, GAME_TYPE_LABEL_SPACING = 64;
  public static final int PAUSE_TEXT_X = 14, PAUSE_TEXT_Y = 7;
  public static final int MUSIC_LABEL_X = 24, MUSIC_LABEL_Y = 96;
  public static final int MUSIC_COLUMN_SPACING = 64, MUSIC_ROW_SPACING = 16;
}

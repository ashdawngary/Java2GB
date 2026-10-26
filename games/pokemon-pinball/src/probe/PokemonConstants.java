package probe;

public final class PokemonConstants {
  private PokemonConstants() {}
  public static final int BYTE_MASK = 255, WORD_MASK = 65535, FRACTION_BITS = 8;
  public static final int GRAVITY = 11, FLIPPER_STEP = 0x333, FLIPPER_LAST = 15;
  public static final int FLIPPER_LIMIT = 16 << FRACTION_BITS, FLIPPER_CLAMP = 15 << FRACTION_BITS;
  public static final int FLIPPER_X = 43, FLIPPER_Y = 123, FLIPPER_WIDTH = 48, FLIPPER_HEIGHT = 32;
  public static final int FLIPPER_STATE_BYTES = FLIPPER_WIDTH * FLIPPER_HEIGHT;
  public static final int FIELD_CENTER_X = 80, FIELD_MIRROR_X = 160;
  public static final int RED_TOP = 0, RED_BOTTOM = 1, BLUE_TOP = 4, BLUE_BOTTOM = 5;
  public static final int LAUNCH_X = 167, LAUNCH_Y = 152;
  public static final int RED_LAUNCH_SPEED = -0x580, BLUE_LAUNCH_SPEED = -0x58f;
  public static final int COLLISION_POINTS = 16, COLLISION_POINT_MASK = COLLISION_POINTS - 1;
  public static final int TILE_BITS = 3, TILE_SIZE = 1 << TILE_BITS, TILE_MASK = TILE_SIZE - 1;
  public static final int MAP_STRIDE = 32, MAP_ROWS = 24;
  public static final int ANIMATED_MASK_FIRST = 0xd0, FLIPPER_MASK_FIRST = 0xe0;
  public static final int RIGHT_MASK_FLAG = 0x10, MASK_ID_MASK = 0x0f;
  public static final int FLIPPER_MIDDLE = 7, FLIPPER_UP = 14, MASK_FRAME_BYTES = 128;
  public static final int VIEW_CHANGE_Y = 136, UPPER_EXIT_Y = 8, LOWER_EXIT_Y = 168;
  public static final int SCROLL_MAX = 32, SCROLL_STEP = 2, SCROLL_BALL_X = 154;
  public static final int INITIAL_BALLS = 3, BUMPER_POINTS = 500, SLING_POINTS = 50;
  public static final int TITLE_SCENE = 0, FIELD_SELECT_SCENE = 1, GAME_SCENE = 2, OVER_SCENE = 3;
  public static final int SCENES_PER_MODEL = 7;
  public static final int TITLE_ASSET = 0, SELECT_RED_ASSET = 1, SELECT_BLUE_ASSET = 2;
  public static final int RED_BOTTOM_ASSET = 3, RED_TOP_ASSET = 4, BLUE_BOTTOM_ASSET = 5, BLUE_TOP_ASSET = 6;
  public static final int OAM_SLOTS = 40, DYNAMIC_OAM_SLOTS = 8, BALL_FRAMES = 8, BALL_FRAME_SHIFT = 4;
  public static final int RIGHT_FLIPPER_FRAME = 8, LEFT_FLIPPER_FRAME = 11;
  public static final int RIGHT_FLIPPER_ANCHOR = 104, LEFT_FLIPPER_ANCHOR = 56, FLIPPER_ANCHOR_Y = 123;
  public static final int STATUS_SPLIT_LINE = 131, FIELD_LCD = 0xe7, STATUS_LCD = 0xf7;
}

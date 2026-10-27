package dev.java2gb.api;

import org.teavm.interop.Import;
import org.teavm.interop.c.Include;

@Include("probe_bridge.h")
public final class GameBoy {
    private GameBoy() {}
    public static final int RIGHT = 1, LEFT = 2, UP = 4, DOWN = 8, A = 16, B = 32, SELECT = 64, START = 128;
    @Import(name = "probe_init") public static native void init();
    @Import(name = "probe_tile_row") public static native void tileRow(int row, int low, int high);
    @Import(name = "probe_show") public static native void show();
    @Import(name = "probe_buttons") public static native int buttons();
    @Import(name = "probe_move_sprite") public static native void moveSprite(int x, int y);
    @Import(name = "probe_sprite") public static native void sprite(int index, int x, int y);
    @Import(name = "probe_hide_sprites") public static native void hideSprites(int first, int end);
    @Import(name = "probe_wait_frame") public static native void waitFrame();
    @Import(name = "probe_sprite_chain") public static native void spriteChain(int first, int x, int y, int dx, int dy, int count);
    @Import(name = "probe_sprite_flip") public static native void spriteFlip(int index, int flip);
    @Import(name = "probe_load_tiles") public static native void loadTileSet();
    @Import(name = "probe_text_tile") public static native int textTile(String text, int column, boolean centered);
    @Import(name = "probe_text_width") public static native int textWidth(String text);
    @Import(name = "probe_define_tile") public static native void defineTile(int tile, int row, int low, int high);
    @Import(name = "probe_background") public static native void background(int x, int y, int tile);
    @Import(name = "probe_background_pair") public static native void backgroundPair(int x, int y, int firstTile);
    @Import(name = "probe_background_column") public static native void backgroundColumn(int x, int y, int packedTiles);
    @Import(name = "probe_sprite_tile") public static native void spriteTile(int index, int tile);
    @Import(name = "probe_display") public static native void display();
    @Import(name = "probe_scroll") public static native void scroll(int x, int y);
    @Import(name = "probe_window") public static native void window(int x, int y, int tile);
    @Import(name = "probe_hud") public static native void hud(int screenY);
    @Import(name = "probe_viewport") public static native void viewport(int height);
    @Import(name = "probe_save_open") public static native void saveOpen();
    @Import(name = "probe_save_read") public static native int saveRead(int offset);
    @Import(name = "probe_save_write") public static native void saveWrite(int offset, int value);
    @Import(name = "probe_save_close") public static native void saveClose();
    @Import(name = "probe_report") public static native void report(int value);
}

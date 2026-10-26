#include "probe_bridge.h"
#include <stdio.h>
void probe_init(void) {}
void probe_tile_row(int32_t row, int32_t low, int32_t high) { (void)row; (void)low; (void)high; }
void probe_show(void) {}
int32_t probe_buttons(void) { return 0; }
void probe_move_sprite(int32_t x, int32_t y) { (void)x; (void)y; }
void probe_wait_frame(void) {}
void probe_report(int32_t value) { printf("%ld\n", (long)value); }

void probe_sprite(int32_t index, int32_t x, int32_t y) { (void)index; (void)x; (void)y; }

void probe_hide_sprites(int32_t first, int32_t end) { (void)first; (void)end; }

void probe_define_tile(int32_t tile, int32_t row, int32_t low, int32_t high) { (void)tile; (void)row; (void)low; (void)high; }
void probe_background(int32_t x, int32_t y, int32_t tile) { (void)x; (void)y; (void)tile; }
void probe_sprite_tile(int32_t index, int32_t tile) { (void)index; (void)tile; }
void probe_display(void) {}

void probe_sprite_chain(int32_t first, int32_t x, int32_t y, int32_t dx, int32_t dy, int32_t count) { (void)first; (void)x; (void)y; (void)dx; (void)dy; (void)count; }

void probe_scroll(int32_t x, int32_t y) { (void)x; (void)y; }
void probe_window(int32_t x, int32_t y, int32_t tile) { (void)x; (void)y; (void)tile; }
void probe_hud(int32_t screen_y) { (void)screen_y; }
void probe_viewport(int32_t height) { (void)height; }

void probe_background_pair(int32_t x, int32_t y, int32_t tile) { (void)x; (void)y; (void)tile; }

void probe_background_column(int32_t x, int32_t y, int32_t tiles) { (void)x; (void)y; (void)tiles; }

void probe_sprite_flip(int32_t index, int32_t flip) { (void)index; (void)flip; }

void probe_load_tiles(void) {}

void probe_save_open(void) {}
int32_t probe_save_read(int32_t offset) { (void)offset; return 255; }
void probe_save_write(int32_t offset, int32_t value) { (void)offset; (void)value; }
void probe_save_close(void) {}

int32_t probe_text_tile(void* text, int32_t column, int32_t centered) { (void)text; (void)column; (void)centered; return 0; }
int32_t probe_text_width(void* text) { (void)text; return 0; }

#ifndef PROBE_BRIDGE_H
#define PROBE_BRIDGE_H
#include <stdint.h>
void probe_init(void);
void probe_tile_row(int32_t row, int32_t low, int32_t high);
void probe_show(void);
int32_t probe_buttons(void);
void probe_move_sprite(int32_t x, int32_t y);
void probe_sprite(int32_t index, int32_t x, int32_t y);
void probe_hide_sprites(int32_t first, int32_t end);
void probe_wait_frame(void);
void probe_sprite_chain(int32_t first, int32_t x, int32_t y, int32_t dx, int32_t dy, int32_t count);
void probe_sprite_flip(int32_t index, int32_t flip);
#ifdef PROBE_BANKED_ROM
void probe_load_tiles(void) __banked;
#else
void probe_load_tiles(void);
#endif
int32_t probe_text_tile(void* text, int32_t column, int32_t centered);
int32_t probe_text_width(void* text);
void probe_define_tile(int32_t tile, int32_t row, int32_t low, int32_t high);
void probe_background(int32_t x, int32_t y, int32_t tile);
void probe_background_pair(int32_t x, int32_t y, int32_t tile);
void probe_background_column(int32_t x, int32_t y, int32_t tiles);
void probe_sprite_tile(int32_t index, int32_t tile);
void probe_display(void);
void probe_scroll(int32_t x, int32_t y);
void probe_window(int32_t x, int32_t y, int32_t tile);
void probe_hud(int32_t screen_y);
void probe_viewport(int32_t height);
void probe_save_open(void);
int32_t probe_save_read(int32_t offset);
void probe_save_write(int32_t offset, int32_t value);
void probe_save_close(void);
void probe_report(int32_t value);
#endif

#include "probe_bridge.h"
#include "jgb_runtime.h"
static uint8_t probe_view_height;
static volatile uint8_t probe_scroll_x, probe_scroll_y;
static uint8_t probe_scroll_hook;
static void probe_commit_scroll(void) { move_bkg(probe_scroll_x, probe_scroll_y); }
void probe_init(void) { gb_init(); probe_view_height = 0; probe_scroll_x = probe_scroll_y = 0; HIDE_WIN; move_bkg(0,0); }
#ifndef PROBE_NO_LEGACY_TILES
void probe_tile_row(int32_t row, int32_t low, int32_t high) { gb_tile_row(row, low, high); }
void probe_show(void) { uint8_t i; gb_show(); for (i = 0; i < 40; ++i) set_sprite_tile(i, 1); }
#endif
int32_t probe_buttons(void) { return gb_buttons(); }
#ifndef PROBE_NO_LEGACY_TILES
void probe_move_sprite(int32_t x, int32_t y) { gb_move_sprite(x, y); }
#endif
void probe_wait_frame(void) { gb_wait_frame(); }
#ifndef PROBE_NO_REPORT
void probe_report(int32_t value) { gb_report(value); }
#endif

void probe_sprite(int32_t index, int32_t x, int32_t y) {
    if ((uint32_t)index >= 40UL) j_trap(2);
    if (probe_view_height && ((uint32_t)y > (uint32_t)(probe_view_height - 8))) hide_sprite((uint8_t)index);
    else move_sprite((uint8_t)index, (uint8_t)(x + 8), (uint8_t)(y + 16));
}

void probe_hide_sprites(int32_t first, int32_t end) {
    uint8_t i;
    if ((uint32_t)end > 40UL || (uint32_t)first > (uint32_t)end) j_trap(2);
    for (i = (uint8_t)first; i < (uint8_t)end; ++i) hide_sprite(i);
}

void probe_define_tile(int32_t tile, int32_t row, int32_t low, int32_t high) {
    uint16_t offset;
    if ((uint32_t)tile >= 128UL || (uint32_t)row >= 8UL) j_trap(2);
    offset = ((uint16_t)tile << 4) + ((uint16_t)row << 1);
    set_vram_byte((uint8_t*)(0x8000U + offset), (uint8_t)low);
    set_vram_byte((uint8_t*)(0x8001U + offset), (uint8_t)high);
    set_vram_byte((uint8_t*)(0x9000U + offset), (uint8_t)low);
    set_vram_byte((uint8_t*)(0x9001U + offset), (uint8_t)high);
}
void probe_background(int32_t x, int32_t y, int32_t tile) {
    if ((uint32_t)x >= 20UL || (uint32_t)y >= 32UL || (uint32_t)tile >= 256UL) j_trap(2);
    set_bkg_tile_xy((uint8_t)x, (uint8_t)y, (uint8_t)tile);
}
void probe_sprite_tile(int32_t index, int32_t tile) {
    if ((uint32_t)index >= 40UL || (uint32_t)tile >= 128UL) j_trap(2);
    set_sprite_tile((uint8_t)index, (uint8_t)tile);
}
void probe_display(void) { SHOW_BKG; SHOW_SPRITES; DISPLAY_ON; }

void probe_sprite_chain(int32_t first, int32_t x, int32_t y, int32_t dx, int32_t dy, int32_t count) {
    uint8_t i, end, sx, sy;
    int16_t clip_y;
    if ((uint32_t)first > 40UL || (uint32_t)count > 40UL - (uint32_t)first) j_trap(2);
    end = (uint8_t)(first + count);
    sx = (uint8_t)((uint32_t)x + 8UL); sy = (uint8_t)((uint32_t)y + 16UL);
    clip_y = (int16_t)y;
    for (i = (uint8_t)first; i < end; ++i) {
        if (probe_view_height && ((uint16_t)clip_y > (uint16_t)(probe_view_height - 8))) hide_sprite(i);
        else move_sprite(i, sx, sy);
        clip_y += (int16_t)dy;
        sx += (uint8_t)dx; sy += (uint8_t)dy;
    }
}

void probe_scroll(int32_t x, int32_t y) {
    probe_scroll_x = (uint8_t)x; probe_scroll_y = (uint8_t)y;
    if (!probe_scroll_hook) { add_VBL(probe_commit_scroll); probe_scroll_hook = 1; }
    if (!(LCDC_REG & 0x80)) probe_commit_scroll();
}
void probe_window(int32_t x, int32_t y, int32_t tile) {
    if ((uint32_t)x >= 20UL || (uint32_t)y >= 18UL || (uint32_t)tile >= 256UL) j_trap(2);
    set_win_tile_xy((uint8_t)x, (uint8_t)y, (uint8_t)tile);
}
void probe_hud(int32_t screen_y) {
    if ((uint32_t)screen_y > 144UL) j_trap(2);
    LCDC_REG |= 0x40;  
    fill_win_rect(0,0,20,2,0);
    move_win(7,(uint8_t)screen_y);
    if (screen_y == 144) HIDE_WIN; else SHOW_WIN;
}
void probe_viewport(int32_t height) {
    if ((uint32_t)height - 8UL > 136UL) j_trap(2);
    probe_view_height = (uint8_t)height;
}

void probe_background_pair(int32_t x, int32_t y, int32_t tile) {
    if ((uint32_t)x >= 19UL || (uint32_t)y >= 32UL || (uint32_t)tile >= 127UL) j_trap(2);
    set_bkg_tile_xy((uint8_t)x, (uint8_t)y, (uint8_t)tile);
    set_bkg_tile_xy((uint8_t)(x+1), (uint8_t)y, (uint8_t)(tile+1));
}

void probe_background_column(int32_t x, int32_t y, int32_t tiles) {
    uint32_t packed = (uint32_t)tiles;
    if ((uint32_t)x >= 20UL || (uint32_t)y >= 30UL || (packed & 0xff808080UL)) j_trap(2);
    set_bkg_tile_xy((uint8_t)x, (uint8_t)y, (uint8_t)packed);
    set_bkg_tile_xy((uint8_t)x, (uint8_t)(y+1), (uint8_t)(packed >> 8));
    set_bkg_tile_xy((uint8_t)x, (uint8_t)(y+2), (uint8_t)(packed >> 16));
}

void probe_sprite_flip(int32_t index, int32_t flip) {
    if ((uint32_t)index >= 40UL || (uint32_t)flip >= 4UL) j_trap(2);
    set_sprite_prop((uint8_t)index, (uint8_t)flip << 5);
}

#if defined(PROBE_TILE_ASSETS) && !defined(PROBE_BANKED_ROM)
#include "tile_assets.h"
void probe_load_tiles(void) {
    uint16_t i;
    for (i = 0; i < PROBE_ASSET_TILE_COUNT; ++i) {
        set_bkg_data(probe_asset_ids[i],1,probe_asset_pixels[i]);
        set_sprite_data(probe_asset_ids[i],1,probe_asset_pixels[i]);
    }
}
#endif

#ifdef PROBE_TEXT_ASSETS
#include "text_assets.h"
static uint16_t probe_text_index(void* text) {
    uint16_t id = (uint16_t)(uintptr_t)text;
    if (!id) j_trap(6);
    if (id > PROBE_TEXT_COUNT) j_trap(8);
    return id - 1;
}
int32_t probe_text_width(void* text) { return probe_text_widths[probe_text_index(text)]; }
int32_t probe_text_tile(void* text, int32_t column, int32_t centered) {
    uint16_t index = probe_text_index(text);
    if ((uint32_t)column >= 20UL || (uint32_t)centered > 1UL) j_trap(2);
    return probe_text_tiles[index][(uint8_t)centered][(uint8_t)column];
}
#endif

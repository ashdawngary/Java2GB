#include "jgb_runtime.h"

volatile uint8_t jgb_test_done;
volatile uint32_t jgb_test_result;
volatile uint8_t jgb_fault;
static uint8_t tile[16];

void gb_init(void) {
    uint8_t i;
    DISPLAY_OFF;
    jgb_test_done = 0;
    jgb_fault = 0;
    SPRITES_8x8;
    BGP_REG = 0xe4;
    OBP0_REG = 0xe4;
    for (i = 0; i != 40; ++i) hide_sprite(i);
    for (i = 0; i != 16; ++i) tile[i] = 0;
    set_bkg_data(0, 1, tile);
    fill_bkg_rect(0, 0, 32, 32, 0);
}
#ifndef PROBE_NO_LEGACY_TILES
void gb_tile_row(uint32_t row, uint32_t low, uint32_t high) {
    if (row >= 8UL) j_trap(2);
    tile[(uint8_t)row * 2] = (uint8_t)low;
    tile[(uint8_t)row * 2 + 1] = (uint8_t)high;
}
void gb_show(void) {
    set_sprite_data(1, 1, tile);
    set_sprite_tile(0, 1);
    SHOW_BKG;
    SHOW_SPRITES;
    DISPLAY_ON;
}
#endif
uint32_t gb_buttons(void) { return joypad(); }
#ifndef PROBE_NO_LEGACY_TILES
void gb_move_sprite(uint32_t x, uint32_t y) {
    move_sprite(0, (uint8_t)(x + 8UL), (uint8_t)(y + 16UL));
}
#endif
void gb_wait_frame(void) { vsync(); }
#ifndef PROBE_NO_REPORT
void gb_report(uint32_t value) {
    jgb_test_result = value;
    jgb_test_done = 1;
}

#endif

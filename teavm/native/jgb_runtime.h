#ifndef JGB_RUNTIME_H
#define JGB_RUNTIME_H
#include <stdint.h>
#include <gb/gb.h>

extern volatile uint8_t jgb_test_done;
extern volatile uint32_t jgb_test_result;
extern volatile uint8_t jgb_fault;
void gb_wait_frame(void);
static void j_trap(uint8_t code) {
    jgb_fault = code;
    for (;;) gb_wait_frame();
}
void gb_init(void);
void gb_tile_row(uint32_t row, uint32_t low, uint32_t high);
void gb_show(void);
uint32_t gb_buttons(void);
void gb_move_sprite(uint32_t x, uint32_t y);
void gb_report(uint32_t value);
#endif

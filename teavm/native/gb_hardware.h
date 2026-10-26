#ifndef GB_HARDWARE_H
#define GB_HARDWARE_H
#include <stdint.h>
int32_t gb_hw_color(void);
void gb_hw_fast(void);
int32_t gb_hw_clock(void);
void gb_hw_lcd_off(void);
int32_t gb_hw_read(int32_t address);
void gb_hw_write(int32_t address, int32_t value);
void gb_hw_oam(int32_t slot, int32_t packed);
void gb_hw_vram4(int32_t address, int32_t packed);
void gb_hw_raster(int32_t line, int32_t before, int32_t after);
void gb_hw_trap(int32_t code);
void gb_hw_vram_fill(int32_t address, int32_t value, int32_t length);
void gb_hw_vram_span(int32_t address, int32_t packed, int32_t length);
void gb_hw_vblank(int32_t enabled);
#endif

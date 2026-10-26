#include <gb/gb.h>
#include <gb/cgb.h>
#include "jgb_runtime.h"
#include "gb_hardware.h"

static volatile uint8_t split_line=255, lcd_before, lcd_after;
static uint8_t raster_installed;
static void commit_lcd(void) {
    if (split_line != 255U) { LCDC_REG=lcd_before; }
}
static void split_lcd(void) {
    if (split_line != 255U && LY_REG == split_line) {
        while (STAT_REG & 3U) {}
        LCDC_REG=lcd_after;
    }
}
int32_t gb_hw_color(void) { return _cpu == CGB_TYPE; }
void gb_hw_fast(void) { if (_cpu == CGB_TYPE) { cpu_fast(); } }
int32_t gb_hw_clock(void) { return sys_time; }
void gb_hw_lcd_off(void) { DISPLAY_OFF; }
int32_t gb_hw_read(int32_t address) {
    if ((uint32_t)address > 0xffffUL) { j_trap(2); }
    return *(volatile uint8_t*)(uint16_t)address;
}
void gb_hw_write(int32_t address, int32_t value) {
    if ((uint32_t)address > 0xffffUL) { j_trap(2); }
    *(volatile uint8_t*)(uint16_t)address=(uint8_t)value;
}
void gb_hw_oam(int32_t slot, int32_t packed) {
    uint8_t *target;
    uint32_t value=(uint32_t)packed;
    if ((uint32_t)slot >= MAX_HARDWARE_SPRITES) { j_trap(2); }
    target=(uint8_t*)&shadow_OAM[(uint8_t)slot];
    target[0]=(uint8_t)value; target[1]=(uint8_t)(value>>8);
    target[2]=(uint8_t)(value>>16); target[3]=(uint8_t)(value>>24);
}
void gb_hw_vram4(int32_t address, int32_t packed) {
    uint32_t value=(uint32_t)packed;
    uint8_t bytes[4];
    if ((uint32_t)address < 0x8000UL || (uint32_t)address > 0x9ffcUL) { j_trap(2); }
    bytes[0]=(uint8_t)value; bytes[1]=(uint8_t)(value>>8);
    bytes[2]=(uint8_t)(value>>16); bytes[3]=(uint8_t)(value>>24);
    vmemcpy((uint8_t*)(uint16_t)address,bytes,4);
}
void gb_hw_raster(int32_t line, int32_t before, int32_t after) {
    if ((uint32_t)line >= 144UL && line != 255) { j_trap(2); }
    if (!raster_installed) {
        add_VBL(commit_lcd); add_LCD(split_lcd); raster_installed=1;
    }
    lcd_before=(uint8_t)before; lcd_after=(uint8_t)after;
    split_line=(uint8_t)line;
    if (line != 255) { LYC_REG=split_line; STAT_REG|=STATF_LYC; IE_REG|=LCD_IFLAG; }
}
void gb_hw_trap(int32_t code) { j_trap((uint8_t)code); }

#include <gb/gb.h>
#include "jgb_runtime.h"
#include "gb_hardware.h"

void gb_hw_vram_fill(int32_t address, int32_t value, int32_t length) {
    if ((uint32_t)address < 0x8000UL || (uint32_t)address >= 0xa000UL ||
            (uint32_t)length > 0xa000UL-(uint32_t)address) { j_trap(2); }
    if (length) { vmemset((void*)(uint16_t)address,(uint8_t)value,(uint16_t)length); }
}
void gb_hw_vram_span(int32_t address, int32_t packed, int32_t length) {
    uint32_t value=(uint32_t)packed;
    uint8_t bytes[4];
    if ((uint32_t)address < 0x8000UL || (uint32_t)address >= 0xa000UL ||
            (uint32_t)length > 4UL || (uint32_t)length > 0xa000UL-(uint32_t)address) { j_trap(2); }
    bytes[0]=(uint8_t)value; bytes[1]=(uint8_t)(value>>8);
    bytes[2]=(uint8_t)(value>>16); bytes[3]=(uint8_t)(value>>24);
    if (length) { vmemcpy((uint8_t*)(uint16_t)address,bytes,(uint16_t)length); }
}
void gb_hw_vblank(int32_t enabled) {
    CRITICAL {
        if (enabled) { IF_REG&=(uint8_t)~VBL_IFLAG; IE_REG|=VBL_IFLAG; }
        else { IE_REG&=(uint8_t)~VBL_IFLAG; }
    }
}

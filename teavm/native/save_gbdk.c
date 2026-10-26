#include "probe_bridge.h"
#include "jgb_runtime.h"
#ifdef PROBE_SAVE_RAM
void probe_save_open(void) { ENABLE_RAM_MBC5; SWITCH_RAM_MBC5(0); }
int32_t probe_save_read(int32_t offset) {
    if ((uint32_t)offset >= 8192UL) j_trap(2);
    return *((volatile uint8_t*)(0xA000U + (uint16_t)offset));
}
void probe_save_write(int32_t offset, int32_t value) {
    if ((uint32_t)offset >= 8192UL || (uint32_t)value > 255UL) j_trap(2);
    *((volatile uint8_t*)(0xA000U + (uint16_t)offset)) = (uint8_t)value;
}
void probe_save_close(void) { DISABLE_RAM_MBC5; }
#endif

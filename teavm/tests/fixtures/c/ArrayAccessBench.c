#include <gb/gb.h>
#include <stdint.h>
#include <stddef.h>
#define COUNT 256L
#define MASK 63L
extern const uint8_t array_bench_banked[64];
const uint8_t array_bench_fixed[64] = {
    3,20,37,54,71,88,105,122,139,156,173,190,207,224,241,2,
    19,36,53,70,87,104,121,138,155,172,189,206,223,240,1,18,
    35,52,69,86,103,120,137,154,171,188,205,222,239,0,17,34,
    51,68,85,102,119,136,153,170,187,204,221,238,255,16,33,50};
struct Array { int32_t header, hash, length; uint8_t data[64]; } ram;
volatile int32_t results[8];
volatile uint8_t done, fault;
volatile int32_t seed;
void trap(uint8_t code) { fault=code; for (;;) {} }
int32_t checked_ram(const struct Array* source, int32_t index) {
    if (!source) trap(6);
    if (index < 0 || index >= source->length) trap(4);
    return source->data[(uint16_t)index];
}
int32_t banked_byte(const uint8_t* source, int32_t length, int32_t index) {
    uint8_t previous,value;
    if (!source) trap(6);
    if (index < 0 || index >= length) trap(4);
    previous=CURRENT_BANK; SWITCH_ROM(2);
    value=source[(uint16_t)index]; SWITCH_ROM(previous);
    return value;
}
int32_t packed_bytes(const uint8_t* source, int32_t length, int32_t index, uint8_t bank) {
    uint8_t previous; uint32_t value;
    if (!source) trap(6);
    if (index < 0 || length < 4L || index > length-4L) trap(4);
    previous=CURRENT_BANK; if (bank) { SWITCH_ROM(bank); }
    source+=(uint16_t)index;
    value=(uint32_t)source[0]|((uint32_t)source[1]<<8)|((uint32_t)source[2]<<16)|((uint32_t)source[3]<<24);
    if (bank) { SWITCH_ROM(previous); }
    return (int32_t)value;
}
int32_t c_index(int32_t start) { int32_t i,sum=0; for(i=0;i<COUNT;i++) sum+=(i+start)&MASK; return sum; }
int32_t c_ram_raw(int32_t start) { int32_t i,sum=0; for(i=0;i<COUNT;i++) sum+=ram.data[(uint16_t)((i+start)&MASK)]; return sum; }
int32_t c_ram_checked(int32_t start) { int32_t i,sum=0; for(i=0;i<COUNT;i++) sum+=checked_ram(&ram,(i+start)&MASK); return sum; }
int32_t c_fixed(int32_t start) { int32_t i,sum=0; for(i=0;i<COUNT;i++) sum+=array_bench_fixed[(uint16_t)((i+start)&MASK)]; return sum; }
int32_t c_banked(int32_t start) { int32_t i,sum=0; for(i=0;i<COUNT;i++) sum+=banked_byte(array_bench_banked,64L,(i+start)&MASK); return sum; }
int32_t c_ram_narrow(int32_t start) { uint16_t i; int32_t sum=0; for(i=0;i<256U;i++) sum+=ram.data[(uint16_t)((i+start)&MASK)]; return sum; }
int32_t c_packed_ram(int32_t start) { int32_t i; uint32_t sum=0; for(i=0;i<COUNT;i+=4L) sum+=(uint32_t)packed_bytes(ram.data,64L,(i+start)&60L,0); return sum; }
int32_t c_packed_rom(int32_t start) { int32_t i; uint32_t sum=0; for(i=0;i<COUNT;i+=4L) sum+=(uint32_t)packed_bytes(array_bench_banked,64L,(i+start)&60L,2); return sum; }
void main(void) {
    uint8_t i;
    DISPLAY_OFF; IE_REG=0;
    ram.length=64L;
    for(i=0;i<64U;i++) ram.data[i]=(uint8_t)(i*17U+3U);
    seed=DIV_REG;
    results[0]=c_index(seed); results[1]=c_ram_raw(seed); results[2]=c_ram_checked(seed);
    results[3]=c_fixed(seed); results[4]=c_banked(seed); results[5]=c_ram_narrow(seed);
    results[6]=c_packed_ram(seed); results[7]=c_packed_rom(seed);
    done=1; for(;;) {}
}

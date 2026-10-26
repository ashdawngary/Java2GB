#include <gb/gb.h>
#include <string.h>
#include "rom_memory.h"
#include "rom_arrays_table.c"

#ifdef GB_ROM_ARRAY_FIXED_BOUNDS
int32_t gb_rom_array_fixed_bounds(void* array, int32_t index, int32_t length) {
    if (!array) planned_fault(6);
    if (index < 0 || index >= length) planned_fault(4);
    return index;
}
#endif

#ifdef GB_ROM_ARRAY_GENERIC
int32_t gb_rom_array_length(void* array) {
    if (!array) planned_fault(6);
    if (GB_IS_ROM_ARRAY(array))
        return gb_rom_arrays[(uintptr_t)array - 0x1001U].length;
    return ((TeaVM_Array*)array)->size;
}
int32_t gb_rom_array_bounds(void* array, int32_t index) {
    int32_t length = gb_rom_array_length(array);
    if (index < 0 || index >= length) planned_fault(4);
    return index;
}
#endif

#ifdef PROBE_BANKED_ROM
#define GB_ARRAY_BANK_VARS uint8_t previous = CURRENT_BANK; uint8_t switched;
#define GB_ARRAY_BANK_ENTER(table) \
    switched = (table)->bank && (table)->bank != previous; \
    if (switched) { SWITCH_ROM((table)->bank); }
#define GB_ARRAY_BANK_LEAVE() if (switched) { SWITCH_ROM(previous); }
#else
#define GB_ARRAY_BANK_VARS
#define GB_ARRAY_BANK_ENTER(table)
#define GB_ARRAY_BANK_LEAVE()
#endif

#define GB_ARRAY_READER(name, type) \
int32_t gb_rom_array_read_##name(void* array, int32_t index) { \
    const GB_RomArray* table; \
    type value; \
    GB_ARRAY_BANK_VARS \
    gb_rom_array_bounds(array, index); \
    if (!GB_IS_ROM_ARRAY(array)) return TEAVM_ARRAY_AT(array, type, (uint16_t)index); \
    table = &gb_rom_arrays[(uintptr_t)array - 0x1001U]; \
    GB_ARRAY_BANK_ENTER(table) \
    value = ((const type*)table->data)[(uint16_t)index]; \
    GB_ARRAY_BANK_LEAVE() \
    return value; \
}
#ifdef GB_ROM_ARRAY_READ_BYTE
GB_ARRAY_READER(byte, int8_t)
#endif
#ifdef GB_ROM_ARRAY_READ_SHORT
GB_ARRAY_READER(short, int16_t)
#endif
#ifdef GB_ROM_ARRAY_READ_CHAR
GB_ARRAY_READER(char, uint16_t)
#endif
#ifdef GB_ROM_ARRAY_READ_INT
GB_ARRAY_READER(int, int32_t)
#endif

#if defined(GB_ROM_MEMORY_READINTLE) || defined(GB_ROM_MEMORY_COPYTOVRAM) || defined(GB_ROM_MEMORY_COPYTOPORT) || defined(GB_ROM_MEMORY_COPYTOOAM)
static const uint8_t* gb_rom_span(void* source, int32_t offset, int32_t length, uint8_t* bank) {
    int32_t size;
    const uint8_t* data;
    const GB_RomArray* table;
    if (!source) planned_fault(6);
    if (GB_IS_ROM_ARRAY(source)) {
        table=&gb_rom_arrays[(uintptr_t)source-0x1001U];
        size=table->length;
        *bank=table->bank;
        data=(const uint8_t*)table->data;
    } else {
        size=((TeaVM_Array*)source)->size;
        *bank=0;
        data=&TEAVM_ARRAY_AT(source,uint8_t,0);
    }
    if (length < 0 || offset < 0 || length > size || offset > size-length) planned_fault(4);
    return data+(uint16_t)offset;
}
#ifdef PROBE_BANKED_ROM
#define GB_SPAN_ENTER() previous=CURRENT_BANK; if (bank && bank != previous) { SWITCH_ROM(bank); }
#define GB_SPAN_LEAVE() if (bank && bank != previous) { SWITCH_ROM(previous); }
#else
#define GB_SPAN_ENTER() previous=0
#define GB_SPAN_LEAVE()
#endif
#ifdef GB_ROM_MEMORY_READINTLE
int32_t gb_rom_array_read4(void* source, int32_t offset) {
    uint8_t bank,previous;
    const uint8_t* data=gb_rom_span(source,offset,4,&bank);
    uint32_t value;
    GB_SPAN_ENTER();
    value=(uint32_t)data[0] | ((uint32_t)data[1]<<8) | ((uint32_t)data[2]<<16) | ((uint32_t)data[3]<<24);
    GB_SPAN_LEAVE();
    return (int32_t)value;
}
#endif
#ifdef GB_ROM_MEMORY_COPYTOVRAM
void gb_rom_array_vram(void* source, int32_t offset, int32_t address, int32_t plane, int32_t length) {
    uint8_t bank,previous,old_plane=VBK_REG&1U;
    const uint8_t* data=gb_rom_span(source,offset,length,&bank);
    uint8_t* target=(uint8_t*)(uint16_t)address;
    if ((uint32_t)plane > 1UL || (uint32_t)address < 0x8000UL || (uint32_t)address >= 0xa000UL
            || (uint32_t)length > 0xa000UL-(uint32_t)address) planned_fault(2);
    GB_SPAN_ENTER();
    VBK_REG=(uint8_t)plane;
    if (LCDC_REG&0x80U) {
        while (length) {
            uint8_t count=length>16 ? 16 : (uint8_t)length;
            CRITICAL { vmemcpy(target,(uint8_t*)data,count); }
            target+=count; data+=count; length-=count;
        }
    } else memcpy(target,data,(uint16_t)length);
    VBK_REG=old_plane;
    GB_SPAN_LEAVE();
}
#endif
#ifdef GB_ROM_MEMORY_COPYTOPORT
void gb_rom_array_port(void* source, int32_t offset, int32_t port, int32_t length) {
    uint8_t bank,previous;
    const uint8_t* data=gb_rom_span(source,offset,length,&bank);
    uint16_t count=(uint16_t)length;
    if ((uint32_t)port < 0xff00UL || (uint32_t)port > 0xffffUL) planned_fault(2);
    GB_SPAN_ENTER();
    while (count--) { *(volatile uint8_t*)(uint16_t)port=*data++; }
    GB_SPAN_LEAVE();
}
#endif
#ifdef GB_ROM_MEMORY_COPYTOOAM
void gb_rom_array_oam(void* source, int32_t offset) {
    uint8_t bank,previous;
    const uint8_t* data=gb_rom_span(source,offset,160,&bank);
    GB_SPAN_ENTER();
    memcpy((void*)shadow_OAM,data,160);
    GB_SPAN_LEAVE();
}
#endif
#endif

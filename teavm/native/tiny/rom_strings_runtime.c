#include <gb/gb.h>
#include "rom_strings_table.c"
static const GB_RomString* gb_rom_string(void* string) {
    uint16_t id = (uint16_t)(uintptr_t)string;
    if (!id) planned_fault(6);
    if (id > GB_ROM_STRING_COUNT) planned_fault(8);
    return &gb_rom_strings[id-1];
}
int32_t gb_rom_string_length(void* string) { return gb_rom_string(string)->length; }
int32_t gb_rom_string_char_at(void* string, int32_t index) {
    const GB_RomString* text = gb_rom_string(string);
    uint16_t value;
#ifdef PROBE_BANKED_ROM
    uint8_t previous;
#endif
    if (index < 0 || index >= text->length) planned_fault(8);
#ifdef PROBE_BANKED_ROM
    previous = CURRENT_BANK;
    SWITCH_ROM(text->bank);
#endif
    value = text->data[(uint16_t)index];
#ifdef PROBE_BANKED_ROM
    SWITCH_ROM(previous);
#endif
    return value;
}

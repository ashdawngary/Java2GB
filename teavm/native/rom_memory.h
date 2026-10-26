#ifndef GB_ROM_MEMORY_H
#define GB_ROM_MEMORY_H
#include <stdint.h>
int32_t gb_rom_array_read4(void* source, int32_t offset);
void gb_rom_array_vram(void* source, int32_t offset, int32_t address, int32_t plane, int32_t length);
void gb_rom_array_port(void* source, int32_t offset, int32_t port, int32_t length);
void gb_rom_array_oam(void* source, int32_t offset);
#endif

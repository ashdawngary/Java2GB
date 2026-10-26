#include "Core/gb.h"
#include <stdlib.h>

typedef struct CompatEmulator {
    GB_gameboy_t *gb;
    struct CompatEmulator *peer;
    uint64_t ticks;
    unsigned trace_count, trace_bits;
    uint16_t trace_word, trace[32768];
    bool serial_bit, audio_enabled;
    unsigned audio_head, audio_tail;
    int16_t audio[32768];
    uint32_t pixels[160 * 144];
} CompatEmulator;

static const GB_palette_t palette = {{{0, 0, 0}, {85, 85, 85}, {153, 153, 153},
                                      {255, 255, 255}, {255, 255, 255}}};

static uint32_t rgb(GB_gameboy_t *gb, uint8_t r, uint8_t g, uint8_t b) {
    (void)gb;
    return ((uint32_t)r << 16) | ((uint32_t)g << 8) | b;
}

static void sample(GB_gameboy_t *gb, GB_sample_t *value) {
    CompatEmulator *emulator=GB_get_user_data(gb);
    if (!emulator || !emulator->audio_enabled) { return; }
    unsigned next=(emulator->audio_head+2)&32767;
    if (next==emulator->audio_tail) { emulator->audio_tail=(next+2)&32767; }
    emulator->audio[emulator->audio_head]=value->left;
    emulator->audio[(emulator->audio_head+1)&32767]=value->right;
    emulator->audio_head=next;
}

void *compat_open(const char *rom, const char *boot, unsigned model) {
    CompatEmulator *emulator = calloc(1, sizeof(*emulator));
    if (!emulator) { return NULL; }
    emulator->gb = GB_init(GB_alloc(), (GB_model_t)model);
    if (!emulator->gb || GB_load_rom(emulator->gb, rom) || GB_load_boot_rom(emulator->gb, boot)) {
        if (emulator->gb) { GB_dealloc(emulator->gb); }
        free(emulator);
        return NULL;
    }
    GB_set_turbo_mode(emulator->gb, true, true);
    GB_set_user_data(emulator->gb, emulator);
    GB_set_rgb_encode_callback(emulator->gb, rgb);
    GB_set_border_mode(emulator->gb, GB_BORDER_NEVER);
    GB_set_pixels_output(emulator->gb, emulator->pixels);
    GB_set_palette(emulator->gb, &palette);
    GB_set_color_correction_mode(emulator->gb, GB_COLOR_CORRECTION_DISABLED);
    GB_set_emulate_joypad_bouncing(emulator->gb, false);
    GB_set_allow_illegal_inputs(emulator->gb, true);
    GB_set_sample_rate(emulator->gb, 48000);
    GB_apu_set_sample_callback(emulator->gb, sample);
    return emulator;
}

void compat_unlink(CompatEmulator *emulator);

void compat_close(CompatEmulator *emulator) {
    compat_unlink(emulator);
    GB_dealloc(emulator->gb);
    free(emulator);
}
void compat_tick(CompatEmulator *emulator, unsigned frames) {
    while (frames--) { GB_run_frame(emulator->gb); }
}
void compat_keys(CompatEmulator *emulator, unsigned mask) {
    GB_set_key_mask(emulator->gb, (GB_key_mask_t)mask);
}
unsigned compat_read(CompatEmulator *emulator, unsigned address) {
    return GB_safe_read_memory(emulator->gb, address);
}
void compat_write(CompatEmulator *emulator, unsigned address, unsigned value) {
    if (address>=0xc000 && address<0xe000) {
        size_t size;
        uint16_t bank;
        uint8_t *ram=GB_get_direct_access(emulator->gb, GB_DIRECT_ACCESS_RAM, &size, &bank);
        unsigned offset=address<0xd000 ? address-0xc000 : (bank ? bank : 1)*0x1000+(address&0xfff);
        if (offset<size) { ram[offset]=(uint8_t)value; }
        return;
    }
    GB_write_memory(emulator->gb, address, value);
}
void compat_read_block(CompatEmulator *emulator, unsigned address, unsigned size, uint8_t *output) {
    for (unsigned i = 0; i < size; i++) { output[i] = GB_safe_read_memory(emulator->gb, address + i); }
}
const uint32_t *compat_pixels(CompatEmulator *emulator) { return emulator->pixels; }

static void serial_start(GB_gameboy_t *gb, bool bit) {
    CompatEmulator *emulator=GB_get_user_data(gb);
    emulator->serial_bit=bit;
}
static bool serial_end(GB_gameboy_t *gb) {
    CompatEmulator *emulator=GB_get_user_data(gb);
    if (!emulator->peer) { return true; }
    bool bit=GB_serial_get_data_bit(emulator->peer->gb);
    GB_serial_set_data_bit(emulator->peer->gb, emulator->serial_bit);
    emulator->trace_word=(uint16_t)((emulator->trace_word<<1)&0xfefeU)
        | (emulator->serial_bit ? 0x100U : 0U) | (bit ? 1U : 0U);
    if (++emulator->trace_bits==8) {
        if (emulator->trace_count<32768) { emulator->trace[emulator->trace_count++]=emulator->trace_word; }
        emulator->trace_bits=0; emulator->trace_word=0;
    }
    return bit;
}
void compat_unlink(CompatEmulator *emulator) {
    if (!emulator || !emulator->peer) { return; }
    CompatEmulator *peer=emulator->peer;
    emulator->peer=NULL; peer->peer=NULL;
    GB_disconnect_serial(emulator->gb);
    GB_disconnect_serial(peer->gb);
}
void compat_link(CompatEmulator *first, CompatEmulator *second) {
    compat_unlink(first); compat_unlink(second);
    first->peer=second; second->peer=first;
    first->ticks=second->ticks=0;
    GB_set_serial_transfer_bit_start_callback(first->gb, serial_start);
    GB_set_serial_transfer_bit_end_callback(first->gb, serial_end);
    GB_set_serial_transfer_bit_start_callback(second->gb, serial_start);
    GB_set_serial_transfer_bit_end_callback(second->gb, serial_end);
}
void compat_pair_tick(CompatEmulator *first, CompatEmulator *second, unsigned frames) {
    uint64_t target=(first->ticks<second->ticks ? first->ticks : second->ticks)+(uint64_t)frames*140448;
    while (first->ticks<target || second->ticks<target) {
        CompatEmulator *next=first->ticks<=second->ticks ? first : second;
        next->ticks+=GB_run(next->gb);
    }
}
void compat_audio_enable(CompatEmulator *emulator, unsigned enabled) {
    emulator->audio_enabled=enabled!=0;
    emulator->audio_head=emulator->audio_tail=0;
}
unsigned compat_audio_read(CompatEmulator *emulator, int16_t *output, unsigned capacity) {
    unsigned count=0;
    while (count<capacity && emulator->audio_tail!=emulator->audio_head) {
        output[count++]=emulator->audio[emulator->audio_tail];
        emulator->audio_tail=(emulator->audio_tail+1)&32767;
    }
    return count;
}
unsigned compat_vram_read(CompatEmulator *emulator, unsigned address) {
    size_t size;
    uint16_t bank;
    const uint8_t *bytes=GB_get_direct_access(emulator->gb, GB_DIRECT_ACCESS_VRAM, &size, &bank);
    if (address<0x8000 || address>=0xa000 || address-0x8000>=size) { return 0xff; }
    return bytes[address-0x8000];
}

unsigned compat_serial_trace(CompatEmulator *emulator, uint16_t *output, unsigned capacity) {
    unsigned count=emulator->trace_count<capacity ? emulator->trace_count : capacity;
    for (unsigned i=0;i<count;i++) { output[i]=emulator->trace[i]; }
    return count;
}

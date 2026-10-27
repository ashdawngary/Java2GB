#include <gb/gb.h>
#include <stdint.h>
#include <stddef.h>
typedef struct {uint8_t ignored;} TeaVM_Array;
typedef int TeaVM_Class;
#define TEAVM_ALLOC_STACK(n)
#define TEAVM_GC_ROOT_RELEASE(n)
#define TEAVM_GC_ROOT(n,v)
#define TEAVM_RELEASE_STACK
#define TEAVM_WITH_CALL_SITE_ID(n,expr) (expr)
void teavm_initializeClass(TeaVM_Class* cls,void (*function)(void));
void initclass_p_RomArrayMemoryFault(void);
int p_RomArrayMemoryFault_Cls;
void probe_init(void);
int32_t probe_buttons(void);
void probe_report(int32_t);
int32_t gb_rom_array_read4(void*,int32_t);
void gb_rom_array_vram(void*,int32_t,int32_t,int32_t,int32_t);
void gb_rom_array_oam(void*,int32_t);
void gb_rom_array_port(void*,int32_t,int32_t,int32_t);
TeaVM_Array* sfld_p_RomArrayMemoryFault_DATA;
int32_t sfld_p_RomArrayMemoryFault_mode;
int32_t sfld_p_RomArrayMemoryFault_index;
int32_t sfld_p_RomArrayMemoryFault_length;
int32_t sfld_p_RomArrayMemoryFault_destination;
int32_t sfld_p_RomArrayMemoryFault_plane;

const uint8_t bytes[8]={1,2,3,4,5,6,7,8};
volatile uint16_t received;
volatile int32_t received_index;
volatile uint8_t done;
volatile int32_t result;
void teavm_initializeClass(TeaVM_Class* cls,void (*function)(void)) {(void)cls;(void)function; sfld_p_RomArrayMemoryFault_DATA=(TeaVM_Array*)bytes;}
void initclass_p_RomArrayMemoryFault(void) {}
void probe_init(void) {DISPLAY_OFF;}
int32_t probe_buttons(void) {return joypad();}
void probe_report(int32_t value) {result=value;done=1;}
int32_t gb_rom_array_read4(void* source,int32_t offset) {received=(uint16_t)source;received_index=offset;return offset;}
void gb_rom_array_vram(void* source,int32_t offset,int32_t a,int32_t b,int32_t c) {received=(uint16_t)source;received_index=offset+a+b+c;}
void gb_rom_array_oam(void* source,int32_t offset) {received=(uint16_t)source;received_index=offset;}
void gb_rom_array_port(void* source,int32_t offset,int32_t a,int32_t b) {received=(uint16_t)source;received_index=offset+a+b;}
void run(TeaVM_Array* teavm_local_1) {
    int32_t teavm_local_2;
    int32_t teavm_local_3;
    int32_t teavm_local_4;
    int32_t teavm_local_5;
    TEAVM_ALLOC_STACK(INT32_C(1));
    TEAVM_GC_ROOT_RELEASE(INT32_C(0));
    TEAVM_WITH_CALL_SITE_ID(190, teavm_initializeClass((TeaVM_Class*) &p_RomArrayMemoryFault_Cls, &initclass_p_RomArrayMemoryFault));
    TEAVM_WITH_CALL_SITE_ID(191, probe_init());
    while (1) {
        if (!(!TEAVM_WITH_CALL_SITE_ID(192, probe_buttons()))) break;
    }
    teavm_local_2 = sfld_p_RomArrayMemoryFault_mode;
    if ((!teavm_local_2)) {
        teavm_local_1 = sfld_p_RomArrayMemoryFault_DATA;
        teavm_local_3 = sfld_p_RomArrayMemoryFault_index;
        TEAVM_GC_ROOT(INT32_C(0), teavm_local_1);
        teavm_local_3 = TEAVM_WITH_CALL_SITE_ID(193, gb_rom_array_read4(teavm_local_1, teavm_local_3));
        TEAVM_GC_ROOT_RELEASE(INT32_C(0));
        TEAVM_WITH_CALL_SITE_ID(194, probe_report(teavm_local_3));
    } else if ((teavm_local_2 == INT32_C(1))) {
        teavm_local_1 = sfld_p_RomArrayMemoryFault_DATA;
        teavm_local_2 = sfld_p_RomArrayMemoryFault_index;
        teavm_local_4 = sfld_p_RomArrayMemoryFault_destination;
        teavm_local_5 = sfld_p_RomArrayMemoryFault_plane;
        teavm_local_3 = sfld_p_RomArrayMemoryFault_length;
        TEAVM_GC_ROOT(INT32_C(0), teavm_local_1);
        TEAVM_WITH_CALL_SITE_ID(195, gb_rom_array_vram(teavm_local_1, teavm_local_2, teavm_local_4, teavm_local_5, teavm_local_3));
    } else if ((teavm_local_2 == INT32_C(2))) {
        teavm_local_1 = sfld_p_RomArrayMemoryFault_DATA;
        teavm_local_3 = sfld_p_RomArrayMemoryFault_index;
        TEAVM_GC_ROOT(INT32_C(0), teavm_local_1);
        TEAVM_WITH_CALL_SITE_ID(196, gb_rom_array_oam(teavm_local_1, teavm_local_3));
    } else if ((teavm_local_2 != INT32_C(3))) {
        TEAVM_WITH_CALL_SITE_ID(197, probe_report(TEAVM_WITH_CALL_SITE_ID(198, gb_rom_array_read4(NULL, sfld_p_RomArrayMemoryFault_index))));
    } else {
        teavm_local_1 = sfld_p_RomArrayMemoryFault_DATA;
        teavm_local_4 = sfld_p_RomArrayMemoryFault_index;
        teavm_local_3 = sfld_p_RomArrayMemoryFault_destination;
        teavm_local_2 = sfld_p_RomArrayMemoryFault_length;
        TEAVM_GC_ROOT(INT32_C(0), teavm_local_1);
        TEAVM_WITH_CALL_SITE_ID(199, gb_rom_array_port(teavm_local_1, teavm_local_4, teavm_local_3, teavm_local_2));
    }
    TEAVM_WITH_CALL_SITE_ID(200, probe_report(-INT32_C(1)));
    TEAVM_RELEASE_STACK;
}void main(void) {sfld_p_RomArrayMemoryFault_index=5;run(NULL);while(1){}}

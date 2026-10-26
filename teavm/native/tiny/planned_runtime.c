#include "runtime.h"
#include "classes/java/lang/Object.h"
#include "jgb_runtime.h"
#include <string.h>
#include "planned_storage.c"

volatile uint32_t gb_plan_allocation_count;
const uint16_t gb_plan_reserved_bytes = GB_PLAN_BYTES;

static void planned_fault(uint8_t code) {
    jgb_fault = code;
    for (;;) gb_wait_frame();
}
#ifdef GB_ROM_STRINGS
#include "rom_strings_runtime.c"
#endif
#ifdef GB_ROM_ARRAYS
#include "rom_arrays_runtime.c"
#endif
#ifdef GB_BOUNDED_POOLS
#include "pool_runtime.c"
#endif
void* gb_plan_allocate(uint16_t slot, void* raw, int32_t length) {
    TeaVM_Class* cls = raw;
    TeaVM_Object* object;
    uint32_t bytes;
    if (slot >= GB_PLAN_SLOT_COUNT) planned_fault(5);
#ifdef GB_BOUNDED_POOLS
    slot = gb_plan_pool_counts[slot] ? gb_pool_acquire(slot) : gb_plan_first[slot];
#else
    slot = gb_plan_first[slot];
#endif
#ifndef GB_PLAN_NO_ARRAYS
    if (length >= 0) {
        if (!cls->itemType || (cls->size != 1L && cls->size != 2L && cls->size != 4L)) planned_fault(5);
        if (gb_plan_capacities[slot] < sizeof(TeaVM_Array) ||
            (uint32_t)length > (gb_plan_capacities[slot] - sizeof(TeaVM_Array)) / (uint32_t)cls->size)
            planned_fault(5);
        bytes = sizeof(TeaVM_Array) + (uint32_t)length * (uint32_t)cls->size;
    } else
#endif
    {
        if (length != -1L || cls->itemType || cls->size < (int32_t)sizeof(TeaVM_Object)) planned_fault(5);
        bytes = (uint32_t)cls->size;
    }
    if (bytes > gb_plan_capacities[slot]) planned_fault(5);
    object = TEAVM_ALIGN(gb_plan_slots[slot], 4);
    memset(object, 0, gb_plan_capacities[slot]);
    object->header = (int32_t)(uintptr_t)cls;
#ifdef GB_BOUNDED_POOLS
    object->hash = (int32_t)slot + 1L;
#endif
#ifndef GB_PLAN_NO_ARRAYS
    if (length >= 0) ((TeaVM_Array*)object)->size = length;
#endif
    ++gb_plan_allocation_count;
    return object;
}
void teavm_initializeClass(TeaVM_Class* cls, void (*initializer)(void)) {
    if (!(cls->flags & 1L)) initializer();
}
#ifndef GB_PLAN_NO_ARRAYS
int32_t teavm_checkLowerBound(int32_t index) {
    if (index < 0) planned_fault(4);
    return index;
}
int32_t teavm_checkBounds(int32_t index, TeaVM_Array* array) {
    if (!array || index < 0 || index >= array->size) planned_fault(4);
    return index;
}
int32_t teavm_checkUpperBound(int32_t index, TeaVM_Array* array) {
    return teavm_checkBounds(index, array);
}
#endif
void* teavm_nullCheck(void* object) {
    if (!object) planned_fault(6);
    return object;
}
void teavm_gc_writeBarrier(void* object) { (void)object; }
void teavm_registerStaticGcRoots(void*** roots, int count) { (void)roots; (void)count; }
void meth_jl_Object__init_(void* object) { (void)object; }
jl_Object_VT jl_Object_Cls = { .parent = { .size = sizeof(TeaVM_Object) } };

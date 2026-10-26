#include "runtime.h"
#include "classes/java/lang/Object.h"
#include "jgb_runtime.h"
#include <string.h>

static union { uint32_t alignment; uint8_t bytes[1027]; } heap;
volatile uint16_t teavm_gb_heap_used;

static void fault(uint8_t code) {
    jgb_fault = code;
    for (;;) gb_wait_frame();
}
static void* allocate(uint32_t bytes) {
    void* result;
    if (bytes > 1024UL) fault(3);
    bytes = (bytes + 3UL) & ~3UL;
    if (bytes + teavm_gb_heap_used > 1024UL) fault(3);
    result = (uint8_t*)TEAVM_ALIGN(heap.bytes, 4) + teavm_gb_heap_used;
    memset(result, 0, (uint16_t)bytes);
    teavm_gb_heap_used += (uint16_t)bytes;
    return result;
}
void* meth_otr_Allocator_allocate(void* cls) {
    TeaVM_Object* object = allocate(((TeaVM_Class*)cls)->size);
    object->header = (int32_t)(uintptr_t)cls;
    return object;
}
TeaVM_Array* meth_otr_Allocator_allocateArray(void* raw, int32_t length) {
    TeaVM_Class* cls = raw;
    TeaVM_Array* array;
    uint32_t element_size = (uint32_t)cls->size;
    if (length < 0 || !cls->itemType || (element_size != 1UL && element_size != 2UL && element_size != 4UL)) fault(4);
    if ((uint32_t)length > (1024UL - sizeof(TeaVM_Array)) / element_size) fault(3);
    array = allocate(sizeof(TeaVM_Array) + (uint32_t)length * element_size);
    array->parent.header = (int32_t)(uintptr_t)cls;
    array->size = length;
    return array;
}
void teavm_initializeClass(TeaVM_Class* cls, void (*initializer)(void)) {
    if (!(cls->flags & 1L)) initializer();
}
int32_t teavm_checkLowerBound(int32_t index) {
    if (index < 0) fault(4);
    return index;
}
jl_Object_VT jl_Object_Cls = { .parent = { .size = sizeof(TeaVM_Object) } };

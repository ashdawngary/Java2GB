#ifndef TEAVM_GB_TINY_RUNTIME_H
#define TEAVM_GB_TINY_RUNTIME_H
#include <stdint.h>
typedef uint16_t char16_t;
#define TEAVM_TO_CHAR(value) ((int32_t)(uint16_t)(value))
#define TEAVM_TO_BYTE(value) ((int32_t)(int8_t)(value))
#define TEAVM_TO_SHORT(value) ((int32_t)(int16_t)(value))
#include <stddef.h>
#define alignas(n)

typedef struct TeaVM_Object { int32_t header; int32_t hash; } TeaVM_Object;
typedef struct TeaVM_Array { TeaVM_Object parent; int32_t size; } TeaVM_Array;
typedef struct TeaVM_String TeaVM_String;
typedef struct TeaVM_Class {
    TeaVM_Object* classObject;
    struct TeaVM_Class* next;
    int32_t size;
    int32_t flags;
    int32_t tag;
    int32_t modifiers;
    struct TeaVM_Class* itemType;
    struct TeaVM_Class* arrayType;
    int32_t (*isSupertypeOf)(struct TeaVM_Class*, struct TeaVM_Class*);
    struct TeaVM_Class* superclass;
    void* enumValues;
    void* layout;
    void* services;
} TeaVM_Class;

#define TEAVM_CLASS_OF(obj) ((TeaVM_Class*)(uintptr_t)((TeaVM_Object*)(obj))->header)
#define TEAVM_FIELD(ptr, type, name) (((type*)(ptr))->name)
#define TEAVM_ALIGN(addr, alignment) ((void*) (((uintptr_t)(addr) + (alignment) - 1) / (alignment) * (alignment)))
#define TEAVM_ARRAY_LENGTH(array) (((TeaVM_Array*)(array))->size)
#define TEAVM_ARRAY_AT(array, type, index) (((type*)TEAVM_ALIGN(((TeaVM_Array*)(array)) + 1, sizeof(type)))[index])
#define TEAVM_ALLOC_STACK(sz)
#define TEAVM_RELEASE_STACK ((void)0)
#define TEAVM_GC_ROOT(index, ptr) ((void)0)
#define TEAVM_GC_ROOT_RELEASE(index) ((void)0)
#define TEAVM_WITH_CALL_SITE_ID(id, expr) (expr)

extern volatile uint16_t teavm_gb_heap_used;
void teavm_initializeClass(TeaVM_Class* cls, void (*initializer)(void));
int32_t teavm_checkLowerBound(int32_t index);
static int32_t teavm_compare_i32(int32_t a, int32_t b) { return a < b ? -1L : a > b ? 1L : 0L; }
void* meth_otr_Allocator_allocate(void* cls);
TeaVM_Array* meth_otr_Allocator_allocateArray(void* cls, int32_t length);
#endif

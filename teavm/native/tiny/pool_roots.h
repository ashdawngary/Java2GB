#ifndef GB_POOL_ROOTS_H
#define GB_POOL_ROOTS_H
typedef struct GB_RootFrame {
    struct GB_RootFrame* previous;
    uint16_t count;
    void** values;
} GB_RootFrame;
extern GB_RootFrame* gb_root_top;
void gb_root_enter(GB_RootFrame* frame, void** values, uint16_t count);
#undef TEAVM_ALLOC_STACK
#undef TEAVM_RELEASE_STACK
#undef TEAVM_GC_ROOT
#undef TEAVM_GC_ROOT_RELEASE
#define TEAVM_ALLOC_STACK(sz) void* gb_roots[(sz)+1]; GB_RootFrame gb_frame; gb_root_enter(&gb_frame, gb_roots, (sz))
#define TEAVM_RELEASE_STACK (gb_root_top = gb_frame.previous)
#define TEAVM_GC_ROOT(index, ptr) (gb_roots[index] = (void*)(ptr))
#define TEAVM_GC_ROOT_RELEASE(index) (gb_roots[index] = NULL)
void gb_pool_mark(void* object);
void gb_pool_static_roots(void);

#endif

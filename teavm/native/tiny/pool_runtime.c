GB_RootFrame* gb_root_top;
static uint8_t gb_pool_marks[GB_PLAN_CELL_COUNT];
volatile uint32_t gb_pool_scan_count;
volatile uint32_t gb_pool_reuse_count;
volatile uint16_t gb_pool_max_live;
volatile uint16_t gb_pool_last_slot;
volatile uint16_t gb_pool_last_live;

void gb_root_enter(GB_RootFrame* frame, void** values, uint16_t count) {
    uint16_t i;
    frame->previous = gb_root_top;
    frame->count = count;
    frame->values = values;
    for (i = 0; i < count; ++i) values[i] = NULL;
    gb_root_top = frame;
}
void gb_pool_mark(void* object) {
    uint16_t i;
    if (!object) return;
#ifdef GB_ROM_STRINGS
    if ((uintptr_t)object <= GB_ROM_STRING_COUNT) return;
#endif
#ifdef GB_ROM_ARRAYS
    if (GB_IS_ROM_ARRAY(object)) return;
#endif
    i = (uint16_t)((TeaVM_Object*)object)->hash;
    if (!i || i > GB_PLAN_CELL_COUNT) planned_fault(5);
    --i;
    if (object != TEAVM_ALIGN(gb_plan_slots[i], 4)) planned_fault(5);
    if (!gb_pool_marks[i]) gb_pool_marks[i] = 1;
}
static void gb_pool_trace(void) {
    GB_RootFrame* frame;
    uint16_t i, j, live = 0;
    uint8_t pending;
    TeaVM_Object* object;
    TeaVM_Class* cls;
    int16_t* layout;
    memset(gb_pool_marks, 0, sizeof(gb_pool_marks));
    for (frame = gb_root_top; frame; frame = frame->previous)
        for (i = 0; i < frame->count; ++i) gb_pool_mark(frame->values[i]);
    gb_pool_static_roots();
    do {
        pending = 0;
        for (i = 0; i < GB_PLAN_CELL_COUNT; ++i) if (gb_pool_marks[i] == 1) {
            gb_pool_marks[i] = 2;
            ++live;
            pending = 1;
            object = TEAVM_ALIGN(gb_plan_slots[i], 4);
            if (!object->header) planned_fault(5);
            cls = TEAVM_CLASS_OF(object);
            if (!cls->itemType && cls->layout) {
                layout = cls->layout;
                for (j = 1; j <= (uint16_t)layout[0]; ++j)
                    gb_pool_mark(*(void**)((uint8_t*)object + layout[j]));
            }
        }
    } while (pending);
    ++gb_pool_scan_count;
    if (live > gb_pool_max_live) gb_pool_max_live = live;
}
static uint16_t gb_pool_acquire(uint16_t slot) {
    uint16_t i, first = gb_plan_first[slot], end = first + gb_plan_pool_counts[slot];
    gb_pool_trace();
    for (i = first; i < end; ++i) if (!gb_pool_marks[i]) {
        if (((TeaVM_Object*)TEAVM_ALIGN(gb_plan_slots[i], 4))->header) ++gb_pool_reuse_count;
        return i;
    }
    gb_pool_last_slot = slot;
    gb_pool_last_live = end - first;
    planned_fault(7);
    return first;
}

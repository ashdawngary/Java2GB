#ifndef GB_SERIAL_H
#define GB_SERIAL_H
#include <stdint.h>
void gb_serial_open(void);
void gb_serial_close(void);
void gb_serial_arm(int32_t data, int32_t control);
void gb_serial_reload(int32_t data, int32_t flags);
void gb_serial_schedule(int32_t data);
void gb_serial_delay250(void);
int32_t gb_serial_read(void);
#endif

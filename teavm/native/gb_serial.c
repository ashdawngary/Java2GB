#include <gb/gb.h>
#include "gb_serial.h"

#define SERIAL_RX_CAPACITY 256U  
#define SERIAL_RX_MASK (SERIAL_RX_CAPACITY - 1U)
#define SERIAL_REPLY_CLEAR 2U
#define SERIAL_REARM_DELAY 4U
#define SERIAL_DELAY_ITERATIONS 250
#define SERIAL_IDLE_BYTE 0xffU

static volatile uint16_t serial_rx[SERIAL_RX_CAPACITY];
static volatile uint8_t serial_head, serial_tail, serial_next, serial_active;
static volatile uint8_t serial_reload, serial_open, serial_scheduled, serial_clock_byte;
volatile uint16_t gb_serial_overruns;

void gb_serial_delay250(void) __naked {
    __asm
        push bc
        ld b, #SERIAL_DELAY_ITERATIONS
99991$:
        ld b, b
        dec b
        jr nz, 99991$
        pop bc
        ret
    __endasm;
}
static void serial_vblank(void) {
    if (serial_scheduled && !(SC_REG&SIOF_XFER_START)) {
        serial_scheduled=0;
        serial_active=serial_clock_byte;
        SB_REG=serial_active;
        SC_REG=SIOF_XFER_START|SIOF_CLOCK_INT;
    }
}
static void serial_interrupt(void) {
    uint8_t next=(serial_head+1U)&SERIAL_RX_MASK;
    if (next!=serial_tail) {
        serial_rx[serial_head]=((uint16_t)serial_active<<8)|SB_REG;
        serial_head=next;
    } else { gb_serial_overruns++; }
    if (serial_reload) {
        serial_active=serial_next;
        SB_REG=serial_active;
        if (serial_reload&SERIAL_REPLY_CLEAR) { serial_next=SERIAL_IDLE_BYTE; }
        if (serial_reload&SERIAL_REARM_DELAY) {
            enable_interrupts();
            gb_serial_delay250();
        }
        SC_REG=SIOF_XFER_START|SIOF_CLOCK_EXT;
    }
}
void gb_serial_close(void) {
    CRITICAL {
        SC_REG=0;
        IE_REG&=(uint8_t)~SIO_IFLAG;
        if (serial_open) {
            remove_SIO(serial_interrupt);
            remove_SIO(nowait_int_handler);
            remove_VBL(serial_vblank);
        }
        IF_REG&=(uint8_t)~SIO_IFLAG;
        serial_open=serial_scheduled=serial_reload=0;
    }
}
void gb_serial_open(void) {
    gb_serial_close();
    CRITICAL {
        serial_head=serial_tail=0;
        gb_serial_overruns=0;
        serial_next=SERIAL_IDLE_BYTE;
        add_SIO(serial_interrupt);
        add_SIO(nowait_int_handler);
        add_VBL(serial_vblank);
        serial_open=1;
        IF_REG&=(uint8_t)~SIO_IFLAG;
        IE_REG|=SIO_IFLAG;
    }
}
void gb_serial_arm(int32_t data, int32_t control) {
    CRITICAL {
        SC_REG=0;
        serial_active=(uint8_t)data;
        SB_REG=serial_active;
        SC_REG=(uint8_t)control;
    }
}
void gb_serial_reload(int32_t data, int32_t flags) {
    CRITICAL { serial_next=(uint8_t)data; serial_reload=(uint8_t)flags; }
}
void gb_serial_schedule(int32_t data) {
    CRITICAL { serial_clock_byte=(uint8_t)data; serial_scheduled=1; }
}
int32_t gb_serial_read(void) {
    uint16_t value;
    if (serial_head==serial_tail) { return -1; }
    value=serial_rx[serial_tail]; serial_tail=(serial_tail+1U)&SERIAL_RX_MASK;
    return value;
}

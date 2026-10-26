#include <gb/gb.h>
#include "probe_bridge.h"
#pragma bank 255
BANKREF(gb_tile_assets)
#include "tile_assets.h"

void probe_load_tiles(void) BANKED {
    uint16_t i;
    for (i = 0; i < PROBE_ASSET_TILE_COUNT; ++i) {
        set_bkg_data(probe_asset_ids[i], 1, probe_asset_pixels[i]);
        set_sprite_data(probe_asset_ids[i], 1, probe_asset_pixels[i]);
    }
}

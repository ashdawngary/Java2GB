package probe;
import dev.java2gb.api.*;

final class PinballArt {
    private PinballArt() {}
    static int textTile(char character) {
        if (character >= 'A' && character <= 'Z') return 48 + character - 'A';
        if (character >= '0' && character <= '9') return 32 + character - '0';
        return 0;
    }
    static int glyph(int index) {
        switch (index) {
            case 0: return 31599;
            case 1: return 11415;
            case 2: return 29671;
            case 3: return 29647;
            case 4: return 23497;
            case 5: return 31183;
            case 6: return 31215;
            case 7: return 29330;
            case 8: return 31727;
            case 9: return 31695;
            case 10: return 11245;
            case 11: return 27566;
            case 12: return 31015;
            case 14: return 31143;
            case 16: return 31087;
            case 17: return 23533;
            case 18: return 9362;
            case 21: return 18727;
            case 22: return 24557;
            case 23: return 24573;
            case 24: return 31599;
            case 25: return 31716;
            case 27: return 31661;
            case 28: return 31183;
            case 29: return 29842;
            case 30: return 23407;
            case 31: return 23402;
            case 13: return 27502;
            case 15: return 31140;
            case 19: return 4714;
            case 20: return 23469;
            case 26: return 11097;
            case 32: return 23549;
            case 33: return 23213;
            case 34: return 23186;
            case 35: return 29351;
            default: return 0;
        }
    }
    static void solid(int tile, int lo, int hi) {
        for (int row = 0; row < 8; row++) GameBoy.defineTile(tile, row, lo, hi);
    }
    static void load() { GameBoy.loadTileSet(); }
    static void buildAssets() {
        solid(0, 0, 0); solid(1, 255, 255);
        for (int row = 0; row < 8; row++) {
            GameBoy.defineTile(2, row, (row & 3) == 0 ? 0x88 : 0, 0);
            GameBoy.defineTile(3, row, 255, row == 0 || row == 7 ? 255 : 129);
            int bits = (7 << row) & 255;
            GameBoy.defineTile(4, row, bits, bits);
            bits = (0xe0 >> row) & 255;
            GameBoy.defineTile(5, row, bits, bits);
            GameBoy.defineTile(7, row, 255, (row & 1) == 0 ? 0xaa : 0x55);
        }
        for (int quadrant = 0; quadrant < 4; quadrant++) {
            picture(8 + quadrant, quadrant * 8);
            for (int row = 0; row < 8; row++) {
                int gy = (quadrant >> 1) * 8 + row;
                int pixels = 0;
                for (int col = 0; col < 8; col++) {
                    int gx = (quadrant & 1) * 8 + col;
                    int dx = gx < 8 ? 7 - gx : gx - 8;
                    int dy = gy < 8 ? 7 - gy : gy - 8;
                    if (dx + dy <= 7 && (dx + dy >= 5 || gx == 7 || gx == 8 || gy == 7 || gy == 8)) pixels |= 128 >> col;
                }
                GameBoy.defineTile(12 + quadrant, row, pixels, pixels);
            }
        }
        for (int row = 0; row < 8; row++) {
            int outline = row == 0 || row == 7 ? 0x18 : row == 1 || row == 6 ? 0x7e : 0xff;
            int shade = row == 2 ? 0xb1 : row == 3 ? 0xa1 : row == 4 || row == 5 ? 0x81 : outline;
            GameBoy.defineTile(16, row, outline, shade);
            int edge = row == 1 || row == 6 ? 0x7e : row > 1 && row < 6 ? 0xff : 0;
            GameBoy.defineTile(17, row, edge, row == 2 || row == 3 ? 0x81 : edge);
            int lamp = row == 0 || row == 7 ? 0 : row == 1 || row == 6 ? 0x3c : 0x7e;
            GameBoy.defineTile(19, row, lamp, lamp);
            int hollow = row == 2 || row == 5 ? 0x42 : row == 3 || row == 4 ? 0x42 : lamp;
            GameBoy.defineTile(20, row, hollow, hollow);
        }
        for (int row = 0; row < 8; row++) {
            int edge = row == 1 || row == 6 ? 255 : row > 1 && row < 6 ? 128 : 0;
            int fill = row >= 1 && row <= 6 ? 255 : (row == 0 ? 0x88 : 0);
            GameBoy.defineTile(21, row, fill, edge | (row > 1 && row < 6 ? 0x24 : 0));
            edge = row == 1 || row == 6 ? 255 : row > 1 && row < 6 ? 1 : 0;
            GameBoy.defineTile(22, row, fill, edge | (row > 1 && row < 6 ? 0x24 : 0));
            int flat = row == 6 ? 255 : row == 0 || row == 4 ? 0x88 : 0;
            GameBoy.defineTile(23, row, flat, row == 6 ? 255 : 0);
            GameBoy.defineTile(24, row, flat, row == 6 ? 255 : 0);
        }
        for (int quadrant = 0; quadrant < 4; quadrant++) {
            picture(25 + quadrant, 64 + quadrant * 8);
            picture(74 + quadrant, 96 + quadrant * 8);
        }
        for (int row = 0; row < 8; row++) {
            int slit = row >= 2 && row <= 5 ? 0 : 255;
            GameBoy.defineTile(78,row,slit,slit);
            int upper = row < 4 ? 255 : 0;
            int lower = row >= 4 ? 255 : 0;
            GameBoy.defineTile(79,row,upper,upper);
            GameBoy.defineTile(80,row,lower,lower);
        }
        for (int compression = 0; compression < 4; compression++) {
            int cap = compression << 1;
            for (int row = 0; row < 8; row++) {
                int bits = row < cap ? 0 : row == cap || row == 7 ? 255 : (row & 1) == 0 ? 0x24 : 0x42;
                GameBoy.defineTile(compression == 0 ? 18 : 80 + compression,row,bits,bits);
            }
        }
        for (int index = 0; index < 36; index++) {
            int tile = index < 10 ? 32 + index : 48 + index - 10;
            int packed = glyph(index);
            for (int row = 0; row < 8; row++) {
                int bits = row > 0 && row < 6 ? ((packed >> (3 * (5 - row))) & 7) << 3 : 0;
                GameBoy.defineTile(tile, row, bits, bits);
            }
        }
    }
    static void picture(int tile, int baseRow) {
        for (int row = 0; row < 8; row++) {
            int packed = pictureRow(baseRow + row);
            GameBoy.defineTile(tile,row,packed & 255,packed >> 8);
        }
    }
    static int pictureRow(int index) {
        switch (index) {
            case 0: case 22: return 257;
            case 1: case 21: return 3855;
            case 2: case 20: return 6175;
            case 3: case 19: case 112: return 12351;
            case 4: return 24696;
            case 5: case 17: return 17279;
            case 6: case 16: return 18303;
            case 7: return 51199;
            case 9: case 29: return 57568;
            case 10: case 28: case 77: return 12528;
            case 11: case 27: return 6392;
            case 12: return 3196;
            case 13: case 25: return 34044;
            case 14: case 24:  return 50428;
            case 15: case 107: case 123: return 50942;
            case 18: case 68: case 69: case 71: case 80: case 82: return 24703;
            case 26: case 88: return 3324;
            case 65: case 85: return 28784;
            case 66: case 84: return 27772;
            case 67: case 83: return 25471;
            case 70: case 81: return 28543;
            case 76: case 90: return 49344;
            case 78: return 36092;
            case 79: return 1023;
            case 86: return 16448;
            case 89: return 45296;
            case 100: case 114: return 771;
            case 101: return 3087;
            case 102: return 12607;
            case 103: return 49407;
            case 105: case 125: return 3598;
            case 106: case 124: return 13886;
            case 108: case 109: case 111: case 120: case 122: return 1790;
            case 110: case 121: return 63230;
            case 113: return 3343;
            case 126: return 514;
            default: return 0;
        }
    }
    static void image(int x, int y, int base) {
        GameBoy.background(x, y, base); GameBoy.background(x + 1, y, base + 1);
        GameBoy.background(x, y + 1, base + 2); GameBoy.background(x + 1, y + 1, base + 3);
    }
    static void board() {
        for (int y = 0; y < 32; y++) for (int x = 0; x < 20; x++) {
            int tile = x == 0 || x == 19 ? 1 : x == 1 || x == 17 || y == 2 ? 3 : x == 18 ? 0 : 2;
            if (y == 31) tile = x > 7 && x < 11 ? 0 : 7;
            if (y < 2) tile = 7;
            GameBoy.background(x, y, tile);
        }
        for (int i = 0; i < 4; i++) {
            GameBoy.background(2 + i, 26 + i, 4);
            GameBoy.background(16 - i, 26 + i, 5);
        }
        image(4, 6, 8); image(9, 10, 8); image(14, 6, 8);
        image(9, 3, 12);
        GameBoy.backgroundPair(6, 16, 21);
        GameBoy.backgroundPair(12, 20, 21);
        image(3, 23, 25); image(14, 23, 74);
        GameBoy.background(18, 30, 18);
        PinballGame.letter(0, 0, 'S'); PinballGame.letter(1, 0, 'C');
        PinballGame.letter(11, 0, 'B'); PinballGame.letter(12, 0, 'O'); PinballGame.letter(13, 0, 'N');
        PinballGame.letter(17, 0, 'B');
    }
}

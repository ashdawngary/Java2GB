from pathlib import Path
import sys
import unittest

sys.path.insert(0,str(Path(__file__).resolve().parents[1]/'.local/tooling'))
from pinball_text import compose,font_from_tiles


class ProportionalTextTests(unittest.TestCase):
    def setUp(self):
        tiles = {i:bytes(16) for i in range(128)}
        for code,bits in [('I',0x10),('M',0x38)]:
            rows = [0,bits,bits,bits,bits,bits,0,0]
            tiles[48+ord(code)-ord('A')] = bytes(v for row in rows for v in (row,row))
        self.font = font_from_tiles(tiles)

    def test_trims_glyphs_and_uses_proportional_advances(self):
        self.assertEqual(self.font[ord('I')][1],1)
        self.assertEqual(self.font[ord('M')][1],3)
        self.assertEqual(compose(map(ord,'II'),self.font)[0],3)
        self.assertEqual(compose(map(ord,'MM'),self.font)[0],7)
        self.assertEqual(compose(map(ord,'I I'),self.font)[0],6)

    def test_letters_share_tiles_and_cross_their_boundaries(self):
        width,tiles = compose(map(ord,'MMM'),self.font)
        self.assertEqual(width,11)
        self.assertEqual(tiles[0][2:4],bytes([0xee,0xee]))
        self.assertEqual(tiles[1][2:4],bytes([0xe0,0xe0]))
        self.assertTrue(all(tile==bytes(16) for tile in tiles[2:]))

    def test_centering_uses_pixels_and_empty_text_is_blank(self):
        width,tiles = compose(map(ord,'IM'),self.font,True)
        ink = [column*8+x for column,tile in enumerate(tiles) for x in range(8) if tile[2]&(128>>x)]
        self.assertEqual(width,5)
        self.assertEqual(ink,[77,79,80,81])
        width,tiles = compose([],self.font,True)
        self.assertEqual(width,0)
        self.assertTrue(all(tile==bytes(16) for tile in tiles))

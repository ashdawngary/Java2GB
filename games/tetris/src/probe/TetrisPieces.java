package probe;
import dev.java2gb.api.*;
import static probe.TetrisLayout.*;
import static probe.TetrisRules.PIECE_BLOCKS;
public final class TetrisPieces {
 @RomData(bank=0) private static final char[] CELLS = {51864,60821,43398,55636,60056,55653,43396,56469,47768,55633,47768,55633,60841,60841,60841,60841,60824,51589,60824,51589,56489,55684,56489,55684,55960,55957,43397,55685};
 @RomData(bank=0) private static final byte[] TILES = {-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-124,-127,-127,-127,-127,-127,-127,-127,-127,-127,-127,-127,-127,-127,-127,-127,-127,-118,-117,-117,-113,-128,-120,-120,-119,-118,-117,-117,-113,-128,-120,-120,-119,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-125,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-126,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-122,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123,-123};
 public static int cells(final int shape) {
  if (shape < 0 || shape >= CELLS.length) { return 0; }
  return CELLS[shape];
 }
 public static int tile(final int shape,int block) {
  if (shape < 0 || shape >= CELLS.length) { return FIRST_BLOCK_TILE; }
  if (block < 0 || block > PIECE_BLOCKS-2) { block=PIECE_BLOCKS-1; }
  return TILES[shape*PIECE_BLOCKS+block] & BYTE_MASK;
 }
}

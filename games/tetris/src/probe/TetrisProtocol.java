package probe;

public final class TetrisProtocol {
  private TetrisProtocol() {}

  public static final int HOST_HELLO = 0x29, GUEST_HELLO = 0x55;
  public static final int MUSIC_FIRST = 0x1c, MUSIC_LAST = 0x1f, MUSIC_REPLY = 0x39;
  public static final int SELECT_HEIGHTS = 0x50, START_ROUND = 0x60;
  public static final int SETUP_DONE = 0x30, SETUP_REPLY = 0x56;
  public static final int BARRIER_POLL = 0x02;
  public static final int READY_REPLY = 0x44, READY_ACK = 0x20;
  public static final int RESULT_REPLY = 0x34, RESULT_ACK = 0x43;
  public static final int REMATCH_REPLY = 0x27, REMATCH_ACK = 0x79;
  public static final int PAUSED = 0x94, WON = 0x77, LOST = 0xaa;
  public static final int ATTACK_FLAG = 0x80, ATTACK_MASK = 0x7f, ATTACK_LIMIT = 5;

  public static final int PIECE_SEQUENCE_LENGTH = 256;
  public static final int PIECE_SEQUENCE_MASK = PIECE_SEQUENCE_LENGTH - 1;
  public static final int RANDOMIZER_WARMUP = 3;
  public static final int OBJECTIVE_LINES = 30, GRAVITY_LEVEL = 1;
  public static final int MATCH_WINS = 4, MATCH_COMPLETE = 5;
  public static final int WIN_DELAY = 100;
  public static final int LOSS_WIPE_DELAY = 70, LOSS_JINGLE_DELAY = 63;
  public static final int LOSS_DELAY = LOSS_WIPE_DELAY + LOSS_JINGLE_DELAY;
  public static final int RESULT_DELAY = 40;
  public static final int END_ANIMATION_STEPS = 29, END_ANIMATION_FRAME_TIME = 25;
  public static final int MATCH_ANIMATION_DELAY = END_ANIMATION_STEPS * END_ANIMATION_FRAME_TIME;
  public static final int RESULT_TIMER_LIMIT = 800;
  public static final int NO_OUTCOME = 0, LOSS_OUTCOME = 1, WIN_OUTCOME = 2;

  public static final int TITLE = -1, HANDSHAKE = 0, MUSIC = 1, HEIGHTS = 2;
  public static final int READY = 3, PLAY = 4, RESULT = 5, RESULT_BARRIER = 6;
  public static final int REMATCH_BARRIER = 7, RESULT_WAIT = 8;

  public static int receivedByte(final int event) {
    return event < 0 ? -1 : event & TetrisLayout.BYTE_MASK;
  }

  public static int transmittedByte(final int event) {
    return event >>> 8;
  }

  public static int terminalCommand(final int outcome) {
    return outcome == LOSS_OUTCOME ? LOST : WON;
  }
}

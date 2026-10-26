package probe;

public final class TetrisJvm {
  public static void main(final String[] args) {
    TetrisMultiplayerChecks.run();
    System.out.println(TetrisChecks.run());
    System.out.println(TetrisChecks.count);
    System.out.println(TetrisChecks.failures);
  }
}

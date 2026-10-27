package probe;

public final class PrimitiveArraysReference {
  public static void main(String[] args) {
    System.out.println(PrimitiveArrays.compute());
    if (PrimitiveArrays.failures != 0) {
      throw new AssertionError("Primitive storage failures: " + PrimitiveArrays.failures);
    }
    System.out.println(PrimitiveArrays.checks);
  }
}

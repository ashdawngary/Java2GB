package probe;

public final class RomArrayReference {
    public static void main(String[] args) {
        System.out.println(RomArrayProbe.run());
        System.out.println(RomArrayProbe.assertions);
        if (RomArrayProbe.failures != 0) { throw new AssertionError(RomArrayProbe.failures); }
    }
}

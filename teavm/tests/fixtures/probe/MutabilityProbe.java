package probe;
import dev.java2gb.api.*;

public final class MutabilityProbe {
    @RomData(bank = 0) private static final byte[] TABLE = {1, 2, 3};
    static final class Holder {
        final byte[] data;
        Holder(@Mutable final byte[] data) { this.data = data; }
        @Mutable void increment() { data[0]++; }
    }
    static void fill(@Mutable final byte[] destination, final byte value) {
        final byte[] alias = destination;
        for (int i = 0; i < alias.length; i++) { alias[i] = value; }
    }
    static void forward(@Mutable final byte[] destination) { fill(destination, (byte) 7); }
    static void copy(final byte[] source, @Mutable final byte[] destination) {
        for (int i = 0; i < source.length; i++) { destination[i] = source[i]; }
    }
    static int read(final byte[] data) { return data[0]; }
    static byte[] view(final byte[] data) { return data; }
    public static int compute() {
        final byte[] a = new byte[3];
        final byte[] b = new byte[3];
        copy(TABLE, a);
        final int original = read(view(a));
        forward(a);  
        copy(a, a);  
        copy(a, b);
        final Holder holder = new Holder(b);
        holder.increment();
        return original + a[0] * 10 + b[0] * 100 + b[2] * 1000;
    }
    public static void main(final String[] args) { GameBoy.report(compute()); }
}

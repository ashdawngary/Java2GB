package probe;

public final class RomArrayReaders {
    @NoInline public static int packed(final byte[] data, final int index) { return RomMemory.readIntLE(data,index); }
    @NoInline public static void copy(final byte[] data, final int index, final int address, final int plane, final int length) {
        RomMemory.copyToVram(data,index,address,plane,length);
    }
    @NoInline public static int byteAt(byte[] data, int index) { return data[index]; }
    @NoInline public static int shortAt(short[] data, int index) { return data[index]; }
    @NoInline public static int charAt(char[] data, int index) { return data[index]; }
    @NoInline public static int intAt(int[] data, int index) { return data[index]; }
    @NoInline public static boolean flagAt(boolean[] data, int index) { return data[index]; }
}

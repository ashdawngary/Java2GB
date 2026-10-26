package probe;

public final class RomText {
    private RomText() {}

    @Mutable
    public static byte[] ascii(final String text) {
        final byte[] result = new byte[text.length()];
        for (int i = 0; i < result.length; i++) {
            final char value = text.charAt(i);
            if (value > 127) {
                throw new IllegalArgumentException("RomText.ascii requires ASCII characters");
            }
            result[i] = (byte) value;
        }
        return result;
    }

    public static char[] chars(final String text) {
        return text.toCharArray();
    }

    @Mutable
    public static byte[] bytes(final String text) {
        final byte[] result = new byte[text.length()];
        for (int i = 0; i < result.length; i++) {
            final char value = text.charAt(i);
            if (value > 255) {
                throw new IllegalArgumentException("RomText.bytes requires values in 0..255");
            }
            result[i] = (byte) value;
        }
        return result;
    }
}

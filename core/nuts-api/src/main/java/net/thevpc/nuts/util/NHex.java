package net.thevpc.nuts.util;

import java.nio.charset.StandardCharsets;

/**
 * NHex class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NHex {

    private static final byte[] HEX_ARRAY = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
    private static final char[] BASE16_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Checks if is hex digit.
     *
     * @param c c
     * @return is hex digit result
     */
    public static boolean isHexDigit(char c) {
        return c >= '0' && c <= '9'
                || c >= 'a' && c <= 'f'
                || c >= 'A' && c <= 'F';
    }

    /**
     * Converts to byte.
     *
     * @param v v
     * @return to byte result
     */
    public static byte toByte(String v) {
        return toBytes(v, 1)[0];
    }

    /**
     * Converts to short.
     *
     * @param v v
     * @return to short result
     */
    public static short toShort(String v) {
        byte[] b = toBytes(v, 2);
        // Mask with 0xFF to treat as unsigned during promotion
      /**
       * Return.
       *
       * @param 0xFF) 0x ff)
       */
        return (short) (((b[0] & 0xFF) << 8) | (b[1] & 0xFF));
    }

    /**
     * Converts to u short.
     *
     * @param v v
     * @return to u short result
     */
    public static int toUShort(String v) {
        byte[] b = toBytes(v, 2);
        int ch1 = b[0];
        int ch2 = b[1];
      /**
       * Return.
       *
       * @param 0 0
       */
        return (ch1 << 8) + (ch2 << 0);
    }

    /**
     * Converts to int.
     *
     * @param v v
     * @return to int result
     */
    public static int toInt(String v) {
        byte[] b = toBytes(v, 4);
        return ((b[0] & 0xFF) << 24) |
                ((b[1] & 0xFF) << 16) |
                ((b[2] & 0xFF) << 8)  |
                ((b[3] & 0xFF));
    }

    /**
     * Converts to long.
     *
     * @param v v
     * @return to long result
     */
    public static long toLong(String v) {
        byte[] b = toBytes(v, 8);
      /**
       * Return.
       *
       * @param 0 0
       */
        return ((long) b[0] << 56) + ((long) (b[1] & 255) << 48) + ((long) (b[2] & 255) << 40) + ((long) (b[3] & 255) << 32) + ((long) (b[4] & 255) << 24) + (long) ((b[5] & 255) << 16) + (long) ((b[6] & 255) << 8) + (long) ((b[7] & 255) << 0);
    }

    /**
     * From byte.
     *
     * @param a a
     * @return from byte result
     */
    public static String fromByte(byte a) {
        /**
         * From bytes.
         *
         * @param byte[]{a} byte[]{a}
         * @return from bytes result
         */
        return fromBytes(new byte[]{a});
    }

    /**
     * From int.
     *
     * @param v v
     * @return from int result
     */
    public static String fromInt(int v) {
        return fromBytes(new byte[]{
                (byte) (v >>> 24 & 255),
                (byte) (v >>> 16 & 255),
                (byte) (v >>> 8 & 255),
                (byte) (v >>> 0 & 255)
        });
    }

    /**
     * From long.
     *
     * @param v v
     * @return from long result
     */
    public static String fromLong(long v) {
        return fromBytes(new byte[]{
                (byte) ((int) (v >>> 56)),
                (byte) ((int) (v >>> 48)),
                (byte) ((int) (v >>> 40)),
                (byte) ((int) (v >>> 32)),
                (byte) ((int) (v >>> 24)),
                (byte) ((int) (v >>> 16)),
                (byte) ((int) (v >>> 8)),
                (byte) ((int) (v >>> 0))
        });
    }

    /**
     * From short.
     *
     * @param v v
     * @return from short result
     */
    public static String fromShort(short v) {
        return fromBytes(new byte[]{
                (byte) (v >>> 8 & 255),
                (byte) (v >>> 0 & 255)
        });
    }

    /**
     * From bytes.
     *
     * @param bytes bytes
     * @return from bytes result
     */
    public static String fromBytes(byte[] bytes) {
        if(bytes==null){
            return null;
        }
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    /**
     * From bytes.
     *
     * @param bytes bytes
     * @param offset offset
     * @param length length
     * @return from bytes result
     */
    public static String fromBytes(byte[] bytes, int offset, int length) {
        if (bytes == null) {
            return null;
        }
        byte[] hexChars = new byte[length * 2];
        for (int j = 0; j < length; j++) {
            // Read from source + offset, but write to hexChars starting at 0
            int v = bytes[offset + j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    /**
     * Converts to bytes.
     *
     * @param s s
     * @param size size
     * @return to bytes result
     */
    public static byte[] toBytes(String s, int size) {
        if(s==null){
            return null;
        }
        byte[] a = toBytes(s);
        if (a.length != size) {
            /**
             * Illegal argument exception.
             *
             * @param size size
             * @return illegal argument exception result
             */
            throw new IllegalArgumentException("invalid hex " + a.length + " <> " + size);
        }
        return a;
    }

    /**
     * Converts to bytes.
     *
     * @param s s
     * @return to bytes result
     */
    public static byte[] toBytes(String s) {
        if(s==null){
            return null;
        }
        int len = s.length();
        if (len == 0) {
            return new byte[0];
        }
        if (len % 2 == 1) {
            s = s + "0";
            len++;
        }
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            char a = s.charAt(i);
            if (Character.isUpperCase(a)) {
                a = Character.toLowerCase(a);
            }
            char b = s.charAt(i + 1);
            if (Character.isUpperCase(b)) {
                b = Character.toLowerCase(b);
            }
            // using left shift operator on every character
            result[i / 2] = (byte) ((Character.digit(a, 16) << 4)
                    + Character.digit(b, 16));
        }
        return result;
    }


    /**
     * Converts to hex char.
     *
     * @param nibble nibble
     * @return to hex char result
     */
    public static char toHexChar(int nibble) {
        return BASE16_CHARS[nibble & 15];
    }

}

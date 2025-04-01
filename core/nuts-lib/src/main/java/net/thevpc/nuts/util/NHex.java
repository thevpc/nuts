package net.thevpc.nuts.util;

import java.nio.charset.StandardCharsets;

public class NHex {

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    private static final char[] BASE16_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static byte toByte(String v) {
        return toBytes(v, 1)[0];
    }

    public static short toShort(String v) {
        byte[] b = toBytes(v, 2);
        int ch1 = b[0];
        int ch2 = b[1];
        return (short) ((ch1 << 8) + (ch2 << 0));
    }

    public static int toUShort(String v) {
        byte[] b = toBytes(v, 2);
        int ch1 = b[0];
        int ch2 = b[1];
        return (ch1 << 8) + (ch2 << 0);
    }

    public static int toInt(String v) {
        byte[] b = toBytes(v, 4);
        int ch1 = b[0];
        int ch2 = b[1];
        int ch3 = b[2];
        int ch4 = b[3];
        return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
    }

    public static long toLong(String v) {
        byte[] b = toBytes(v, 8);
        return ((long) b[0] << 56) + ((long) (b[1] & 255) << 48) + ((long) (b[2] & 255) << 40) + ((long) (b[3] & 255) << 32) + ((long) (b[4] & 255) << 24) + (long) ((b[5] & 255) << 16) + (long) ((b[6] & 255) << 8) + (long) ((b[7] & 255) << 0);
    }

    public static String fromByte(byte a) {
        return fromBytes(new byte[]{a});
    }

    public static String fromInt(int v) {
        return fromBytes(new byte[]{
                (byte) (v >>> 24 & 255),
                (byte) (v >>> 16 & 255),
                (byte) (v >>> 8 & 255),
                (byte) (v >>> 0 & 255)
        });
    }

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

    public static String fromShort(short v) {
        return fromBytes(new byte[]{
                (byte) (v >>> 8 & 255),
                (byte) (v >>> 0 & 255)
        });
    }

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

    public static String fromBytes(byte[] bytes, int offset, int length) {
        if(bytes==null){
            return null;
        }
        byte[] hexChars = new byte[length * 2];
        for (int j = offset; j < offset + length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    public static byte[] toBytes(String s, int size) {
        if(s==null){
            return null;
        }
        byte[] a = toBytes(s);
        if (a.length != size) {
            throw new IllegalArgumentException("invalid hex " + a.length + " <> " + size);
        }
        return a;
    }

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


    public static char toHexChar(int nibble) {
        return BASE16_CHARS[nibble & 15];
    }

}

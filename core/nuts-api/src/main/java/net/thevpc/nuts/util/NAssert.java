package net.thevpc.nuts.util;

import net.thevpc.nuts.NIllegalArgumentException;

import java.util.Collection;
import java.util.function.Supplier;

public class NAssert {
    private NAssert() {
    }

    private static NMsg createMessage(Supplier<NMsg> msg) {
        requireNonNull(msg, "message supplier");
        NMsg m = msg.get();
        requireNonNull(m, "message");
        return m;
    }

    private static String createName(String name) {
        return NBlankable.isBlank(name) ? "value" : name;
    }

    public static <T> T requireNonNull(T object, Supplier<NMsg> msg) {
        if (object == null) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }


    public static <T> T requireNonNull(T object, String name) {
        return requireNonNull(object, () -> NMsg.ofC("%s should not be null", createName(name)));
    }

    public static <T> T requireNonNull(T object) {
        return requireNonNull(object, "value");
    }


    public static void requireNull(Object object, String name) {
        if (object != null) {
            throw creatIllegalArgumentException(NMsg.ofC("%s must be null", createName(name)));
        }
    }

    public static void requireNull(Object object, Supplier<NMsg> message) {
        if (object != null) {
            throw creatIllegalArgumentException(createMessage(message));
        }
    }


    public static <T> T requireNonBlank(T object, String name) {
        if (NBlankable.isBlank(object)) {
            throw creatIllegalArgumentException(NMsg.ofC("%s should not be blank", createName(name)));
        }
        return object;
    }


    public static <T> T requireNonBlank(T object, Supplier<NMsg> msg) {
        if (NBlankable.isBlank(object)) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }

    private static RuntimeException creatIllegalArgumentException(NMsg m) {
        throw new NIllegalArgumentException(m);
    }

    // NO SESSION


    public static void requireNull(Object object) {
        requireNull(object, (String) null);
    }

    public static boolean requireTrue(boolean value, String name) {
        return requireTrue(value, () -> NMsg.ofC("should be %s", createName(name)));
    }


    public static boolean requireTrue(boolean object, Supplier<NMsg> msg) {
        if (!object) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }


    public static boolean requireFalse(boolean value, String name) {
        return requireFalse(value, () -> NMsg.ofC("should not be %s", createName(name)));
    }

    public static boolean requireFalse(boolean value, Supplier<NMsg> msg) {
        if (!value) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return value;
    }

    public static boolean requireNonEmpty(Collection<?> value, String name) {
        return requireTrue(!value.isEmpty(), () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireEmpty(Collection<?> value, String name) {
        return requireTrue(value.isEmpty(), () -> NMsg.ofC("should be empty %s", createName(name)));
    }

    public static boolean requireNonEmpty(CharSequence value, String name) {
        return requireTrue(value.length() != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireEmpty(CharSequence value, String name) {
        return requireTrue(value.length() == 0, () -> NMsg.ofC("should be empty %s", createName(name)));
    }

    public static boolean requireNonEmpty(Object[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireEmpty(Object[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s", createName(name)));
    }

    public static boolean requireNonEmpty(boolean[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireEmpty(boolean[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s", createName(name)));
    }

    public static boolean requireNonEmpty(byte[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireEmpty(byte[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s", createName(name)));
    }

    public static boolean requireNonEmpty(short[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireEmpty(short[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s", createName(name)));
    }

    public static boolean requireNonEmpty(char[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireEmpty(char[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s", createName(name)));
    }

    public static boolean requireNonEmpty(int[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireEmpty(int[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s", createName(name)));
    }

    public static boolean requireNonEmpty(long[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireEmpty(long[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s", createName(name)));
    }

    public static boolean requireNonEmpty(float[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireEmpty(float[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s", createName(name)));
    }

    public static boolean requireNonEmpty(double[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireEmpty(double[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s", createName(name)));
    }

}

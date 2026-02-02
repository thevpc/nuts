package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

public class NAssert {
    private NAssert() {
    }

    private static NMsg createMessage(Supplier<NMsg> msg) {
        requireNamedNonNull(msg, "message supplier");
        NMsg m = msg.get();
        requireNamedNonNull(m, "message");
        return m;
    }

    private static String createName(String name) {
        return NBlankable.isBlank(name) ? "value" : name;
    }

    public static <T> T requireNonNull(T object, Supplier<NMsg> msg) {
        if (object == null) {
            throw NExceptions.ofSafeAssertException(createMessage(msg));
        }
        return object;
    }


    public static <T> T requireNamedNonNull(T object, String name) {
        return requireNonNull(object, () -> NMsg.ofC("%s should not be null", createName(name)));
    }

    public static <T> T requireNamedNonNull(T object) {
        return requireNamedNonNull(object, "value");
    }


    public static void requireNamedNull(Object object, String name) {
        if (object != null) {
            throw NExceptions.ofSafeAssertException(NMsg.ofC("%s must be null", createName(name)));
        }
    }

    public static void requireNull(Object object, Supplier<NMsg> message) {
        if (object != null) {
            throw NExceptions.ofSafeAssertException(createMessage(message));
        }
    }


    public static <T> T requireNamedNonBlank(T object, String name) {
        if (NBlankable.isBlank(object)) {
            throw NExceptions.ofSafeAssertException(NMsg.ofC("%s should not be blank", createName(name)));
        }
        return object;
    }


    public static <T> T requireNonBlank(T object, Supplier<NMsg> msg) {
        if (NBlankable.isBlank(object)) {
            throw NExceptions.ofSafeAssertException(createMessage(msg));
        }
        return object;
    }



    // NO SESSION


    public static void requireNamedNull(Object object) {
        requireNamedNull(object, (String) null);
    }

    public static boolean requireNamedTrue(boolean value, String name) {
        return requireTrue(value, () -> NMsg.ofC("should be %s", createName(name)));
    }


    public static boolean requireTrue(boolean object, Supplier<NMsg> msg) {
        if (!object) {
            throw NExceptions.ofSafeAssertException(createMessage(msg));
        }
        return object;
    }

    public static <T> T requireEquals(T a, T b, Supplier<NMsg> msg) {
        if (!Objects.equals(a, b)) {
            throw NExceptions.ofSafeAssertException(createMessage(msg));
        }
        return a;
    }

    public static <T> T requireNamedNotEquals(T a, T b, String name) {
        return requireNotEquals(a, b, () -> NMsg.ofC("%s non equality failed", createName(name)));
    }

    public static <T> T requireNotEquals(T a, T b, Supplier<NMsg> msg) {
        if (Objects.equals(a, b)) {
            throw NExceptions.ofSafeAssertException(createMessage(msg));
        }
        return a;
    }

    public static <T> T requireNamedEquals(T a, T b, String name) {
        return requireEquals(a, b, () -> NMsg.ofC("%s equality failed", createName(name)));
    }


    public static boolean requireNamedFalse(boolean value, String name) {
        return requireFalse(value, () -> NMsg.ofC("should not be %s", createName(name)));
    }

    public static boolean requireFalse(boolean value, Supplier<NMsg> msg) {
        if (value) {
            throw NExceptions.ofSafeAssertException(createMessage(msg));
        }
        return value;
    }

    public static boolean requireNamedNonEmpty(Collection<?> value, String name) {
        return requireTrue(!value.isEmpty(), () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireNamedEmpty(Collection<?> value, String name) {
        return requireTrue(value.isEmpty(), () -> NMsg.ofC("should be empty %s, was %s", createName(name),value==null?"null":value.size()));
    }

    public static boolean requireNamedNonEmpty(CharSequence value, String name) {
        return requireTrue(value.length() != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireNamedEmpty(CharSequence value, String name) {
        return requireTrue(value.length() == 0, () -> NMsg.ofC("should be empty %s, was %s", createName(name),value));
    }

    public static boolean requireNamedNonEmpty(Object[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireNamedEmpty(Object[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createName(name),value.length));
    }

    public static boolean requireNamedNonEmpty(boolean[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireNamedEmpty(boolean[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createName(name),value.length));
    }

    public static boolean requireNamedNonEmpty(byte[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireNamedEmpty(byte[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s", createName(name),value.length));
    }

    public static boolean requireNamedNonEmpty(short[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireNamedEmpty(short[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createName(name),value.length));
    }

    public static boolean requireNamedNonEmpty(char[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireNamedEmpty(char[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createName(name),value.length));
    }

    public static boolean requireNamedNonEmpty(int[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireNamedEmpty(int[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createName(name),value.length));
    }

    public static boolean requireNamedNonEmpty(long[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireNamedEmpty(long[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createName(name),value.length));
    }

    public static boolean requireNamedNonEmpty(float[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireNamedEmpty(float[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createName(name),value.length));
    }

    public static boolean requireNamedNonEmpty(double[] value, String name) {
        return requireTrue(value.length != 0, () -> NMsg.ofC("should not be empty %s", createName(name)));
    }

    public static boolean requireNamedEmpty(double[] value, String name) {
        return requireTrue(value.length == 0, () -> NMsg.ofC("should be empty %s, was %s", createName(name),value.length));
    }

}

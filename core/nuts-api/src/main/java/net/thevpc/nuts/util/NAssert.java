package net.thevpc.nuts.util;

import net.thevpc.nuts.NIllegalArgumentException;

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

    public static boolean requireFalse(boolean object, Supplier<NMsg> msg) {
        if (!object) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }
}

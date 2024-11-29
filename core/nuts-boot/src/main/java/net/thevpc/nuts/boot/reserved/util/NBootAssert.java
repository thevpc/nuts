package net.thevpc.nuts.boot.reserved.util;


import java.util.function.Supplier;

public class NBootAssert {
    private NBootAssert() {
    }

    private static NBootMsg createMessage(Supplier<NBootMsg> msg) {
        requireNonNull(msg, "message supplier");
        NBootMsg m = msg.get();
        requireNonNull(m, "message");
        return m;
    }

    private static String createName(String name) {
        return NBootStringUtils.isBlank(name) ? "value" : name;
    }

    public static <T> T requireNonNull(T object, Supplier<NBootMsg> msg) {
        if (object == null) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }


    public static <T> T requireNonNull(T object, String name) {
        return requireNonNull(object, () -> NBootMsg.ofC("%s should not be null", createName(name)));
    }

    public static <T> T requireNonNull(T object) {
        return requireNonNull(object, "value");
    }


    public static void requireNull(Object object, String name) {
        if (object != null) {
            throw creatIllegalArgumentException(NBootMsg.ofC("%s must be null", createName(name)));
        }
    }

    public static void requireNull(Object object, Supplier<NBootMsg> message) {
        if (object != null) {
            throw creatIllegalArgumentException(createMessage(message));
        }
    }


    public static String requireNonBlank(String object, String name) {
        if (NBootStringUtils.isBlank(object)) {
            throw creatIllegalArgumentException(NBootMsg.ofC("%s should not be blank", createName(name)));
        }
        return object;
    }


    public static String requireNonBlank(String object, Supplier<NBootMsg> msg) {
        if (NBootStringUtils.isBlank(object)) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }

    private static RuntimeException creatIllegalArgumentException(NBootMsg m) {
        throw new IllegalArgumentException(m.toString());
    }

    // NO SESSION


    public static void requireNull(Object object) {
        requireNull(object, (String) null);
    }

    public static boolean requireTrue(boolean value, String name) {
        return requireTrue(value, () -> NBootMsg.ofC("should be %s", createName(name)));
    }


    public static boolean requireTrue(boolean object, Supplier<NBootMsg> msg) {
        if (!object) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }


    public static boolean requireFalse(boolean value, String name) {
        return requireFalse(value, () -> NBootMsg.ofC("should not be %s", createName(name)));
    }

    public static boolean requireFalse(boolean object, Supplier<NBootMsg> msg) {
        if (!object) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }
}

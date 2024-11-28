package net.thevpc.nuts.boot.reserved;


import net.thevpc.nuts.boot.reserved.util.NStringUtilsBoot;

import java.util.function.Supplier;

public class NAssertBoot {
    private NAssertBoot() {
    }

    private static NMsgBoot createMessage(Supplier<NMsgBoot> msg) {
        requireNonNull(msg, "message supplier");
        NMsgBoot m = msg.get();
        requireNonNull(m, "message");
        return m;
    }

    private static String createName(String name) {
        return NStringUtilsBoot.isBlank(name) ? "value" : name;
    }

    public static <T> T requireNonNull(T object, Supplier<NMsgBoot> msg) {
        if (object == null) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }


    public static <T> T requireNonNull(T object, String name) {
        return requireNonNull(object, () -> NMsgBoot.ofC("%s should not be null", createName(name)));
    }

    public static <T> T requireNonNull(T object) {
        return requireNonNull(object, "value");
    }


    public static void requireNull(Object object, String name) {
        if (object != null) {
            throw creatIllegalArgumentException(NMsgBoot.ofC("%s must be null", createName(name)));
        }
    }

    public static void requireNull(Object object, Supplier<NMsgBoot> message) {
        if (object != null) {
            throw creatIllegalArgumentException(createMessage(message));
        }
    }


    public static String requireNonBlank(String object, String name) {
        if (NStringUtilsBoot.isBlank(object)) {
            throw creatIllegalArgumentException(NMsgBoot.ofC("%s should not be blank", createName(name)));
        }
        return object;
    }


    public static String requireNonBlank(String object, Supplier<NMsgBoot> msg) {
        if (NStringUtilsBoot.isBlank(object)) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }

    private static RuntimeException creatIllegalArgumentException(NMsgBoot m) {
        throw new IllegalArgumentException(m.toString());
    }

    // NO SESSION


    public static void requireNull(Object object) {
        requireNull(object, (String) null);
    }

    public static boolean requireTrue(boolean value, String name) {
        return requireTrue(value, () -> NMsgBoot.ofC("should be %s", createName(name)));
    }


    public static boolean requireTrue(boolean object, Supplier<NMsgBoot> msg) {
        if (!object) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }


    public static boolean requireFalse(boolean value, String name) {
        return requireFalse(value, () -> NMsgBoot.ofC("should not be %s", createName(name)));
    }

    public static boolean requireFalse(boolean object, Supplier<NMsgBoot> msg) {
        if (!object) {
            throw creatIllegalArgumentException(createMessage(msg));
        }
        return object;
    }
}

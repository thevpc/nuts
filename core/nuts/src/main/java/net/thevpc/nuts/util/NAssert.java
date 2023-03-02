package net.thevpc.nuts.util;

import net.thevpc.nuts.*;

import java.util.function.Supplier;

public class NAssert {
    private NAssert() {
    }

    public static NSession requireSession(NSession session) {
        if (session == null) {
            throw new NMissingSessionException();
        }
        return session;
    }

    public static NSession requireSession(NSession session, NSession defaultSession) {
        if (session == null) {
            if (defaultSession == null) {
                throw new NMissingSessionException();
            } else {
                throw new NMissingSessionException();
                //new NIllegalArgumentException(defaultSession, NMsg.ofPlain("missing session"));
            }
        }
        return session;
    }

    private static NMsg createMessage(Supplier<NMsg> msg, NSession session) {
        requireNonNull(msg, "message supplier", session);
        NMsg m = msg.get();
        requireNonNull(m, "message", session);
        return m;
    }

    private static String createName(String name) {
        return NBlankable.isBlank(name) ? "value" : name;
    }

    public static <T> T requireNonNull(T object, Supplier<NMsg> msg, NSession session) {
        if (object == null) {
            throw creatIllegalArgumentException(session, createMessage(msg, null));
        }
        return object;
    }


    public static <T> T requireNonNull(T object, String name, NSession session) {
        return requireNonNull(object, () -> NMsg.ofC("%s should not be null", createName(name)), session);
    }

    public static <T> T requireNonNull(T object, NSession session) {
        return requireNonNull(object, "value", session);
    }

    public static void requireNull(Object object, NSession session) {
        requireNull(object, "value", session);
    }

    public static void requireNull(Object object, String name, NSession session) {
        if (object != null) {
            throw creatIllegalArgumentException(session, NMsg.ofC("%s must be null", createName(name)));
        }
    }

    public static void requireNull(Object object, Supplier<NMsg> message, NSession session) {
        if (object != null) {
            throw creatIllegalArgumentException(session, createMessage(message, session));
        }
    }


    public static <T> T requireNonBlank(T object, String name, NSession session) {
        if (NBlankable.isBlank(object)) {
            throw creatIllegalArgumentException(session, NMsg.ofC("%s should not be blank", createName(name)));
        }
        return object;
    }


    public static <T> T requireNonBlank(T object, Supplier<NMsg> msg, NSession session) {
        if (NBlankable.isBlank(object)) {
            throw creatIllegalArgumentException(session, createMessage(msg, session));
        }
        return object;
    }

    private static RuntimeException creatIllegalArgumentException(NSession session, NMsg m) {
        if (session != null) {
            throw new NIllegalArgumentException(session, m);
        } else {
            throw new IllegalArgumentException(m.toString());
        }
    }

    // NO SESSION


    public static void requireNull(Object object) {
        requireNull(object, (String) null, null);
    }

    public static void requireNull(Object object, String name) {
        requireNull(object, name, null);
    }

    public static void requireNull(Object object, Supplier<NMsg> message) {
        requireNull(object, message, null);
    }

    public static <T> T requireNonNull(T object, String name) {
        return requireNonNull(object, name, null);
    }

    public static <T> T requireNonNull(T object, Supplier<NMsg> msg) {
        return requireNonNull(object, msg, null);
    }

    public static <T> T requireNonBlank(T object, String name) {
        return requireNonBlank(object, name, null);
    }

    public static <T> T requireNonBlank(T object, Supplier<NMsg> msg) {
        return requireNonBlank(object, msg, null);
    }
}

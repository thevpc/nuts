package net.thevpc.nuts.util;

import net.thevpc.nuts.*;

import java.util.function.Supplier;

public final class NUtils {
    private NUtils() {
    }

    public static NSession requireSession(NSession session) {
        if (session == null) {
            throw new NMissingSessionException();
        }
        return session;
    }

    public static NSession requireSession(NSession session, NSession defaultSession) {
        if (session == null) {
            throw new NIllegalArgumentException(defaultSession, NMsg.ofPlain("missing session"));
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
        return NBlankable.isBlank(name.isEmpty()) ? "value" : name;
    }

    private static NMsg createMessage(Supplier<NMsg> msg) {
        requireNonNull(msg, "message supplier");
        NMsg m = msg.get();
        requireNonNull(m, "message");
        return m;
    }

    public static void requireNonNull(Object object, Supplier<NMsg> msg, NSession session) {
        if (object == null) {
            throw new NIllegalArgumentException(session, createMessage(msg, session));
        }
    }

    public static void requireNonNull(Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(NMsg.ofCstyle("%s is null", createName(name)).toString());
        }
    }

    public static void requireNonNull(Object object, Supplier<NMsg> msg) {
        if (object == null) {
            throw new IllegalArgumentException(createMessage(msg).toString());
        }
    }

    public static void requireNonNull(Object object, String name, NSession defaultSession) {
        requireNonNull(object, () -> NMsg.ofCstyle("%s should not be null", createName(name)), defaultSession);
    }

    public static void requireNonNull(Object object, NSession session) {
        requireNonNull(object, "value", session);
    }

    public static void requireNull(Object object) {
        requireNull(object, "value");
    }

    public static void requireNull(Object object, String name) {
        if (object != null) {
            throw new IllegalArgumentException(createName(name) + " must be null");
        }
    }

    public static void requireNull(Object object, NSession session) {
        requireNull(object, "value", session);
    }

    public static void requireNull(Object object, String name, NSession session) {
        if (object != null) {
            throw new NIllegalArgumentException(session, NMsg.ofCstyle("%s must be null", createName(name)));
        }
    }

    public static void requireNull(Object object, Supplier<NMsg> message, NSession session) {
        if (object != null) {
            throw new NIllegalArgumentException(session, createMessage(message, session));
        }
    }

    public static void requireNonBlank(Object object, String name) {
        if (NBlankable.isBlank(object)) {
            throw new IllegalArgumentException(createName(name) + " should not be blank");
        }
    }

    public static void requireNonBlank(Object object, String name, NSession session) {
        if (NBlankable.isBlank(object)) {
            throw new NIllegalArgumentException(session, NMsg.ofCstyle("%s should not be blank", createName(name)));
        }
    }

    public static void requireNonBlank(Object object, Supplier<NMsg> msg, NSession session) {
        if (NBlankable.isBlank(object)) {
            throw new NIllegalArgumentException(session, createMessage(msg, session));
        }
    }
}

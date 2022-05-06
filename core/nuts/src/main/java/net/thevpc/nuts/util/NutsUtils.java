package net.thevpc.nuts.util;

import net.thevpc.nuts.*;

import java.util.function.Supplier;

public final class NutsUtils {
    private NutsUtils() {
    }

    public static void requireSession(NutsSession session) {
        if (session == null) {
            throw new NutsMissingSessionException();
        }
    }

    public static void requireSession(NutsSession session, NutsSession defaultSession) {
        if (session == null) {
            throw new NutsIllegalArgumentException(defaultSession, NutsMessage.ofPlain("missing session"));
        }
    }

    private static NutsMessage createMessage(Supplier<NutsMessage> msg, NutsSession session) {
        requireNonNull(msg, session, "message supplier");
        NutsMessage m = msg.get();
        requireNonNull(m, session, "message");
        return m;
    }

    private static String createName(String name) {
        return NutsBlankable.isBlank(name.isEmpty()) ? "value" : name;
    }

    private static NutsMessage createMessage(Supplier<NutsMessage> msg) {
        requireNonNull(msg, "message supplier");
        NutsMessage m = msg.get();
        requireNonNull(m, "message");
        return m;
    }

    public static void requireNonNull(Object object, NutsSession session, Supplier<NutsMessage> msg) {
        if (object == null) {
            throw new NutsIllegalArgumentException(session, createMessage(msg, session));
        }
    }

    public static void requireNonNull(Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(NutsMessage.ofCstyle("%s is null", createName(name)).toString());
        }
    }

    public static void requireNonNull(Object object, Supplier<NutsMessage> msg) {
        if (object == null) {
            throw new IllegalArgumentException(createMessage(msg).toString());
        }
    }

    public static void requireNonNull(Object object, NutsSession defaultSession, String name) {
        requireNonNull(object, defaultSession, () -> NutsMessage.ofCstyle("%s should not be null", createName(name)));
    }

    public static void requireNonNull(Object object, NutsSession session) {
        requireNonNull(object, session, "value");
    }

    public static void requireNull(Object object, NutsSession session) {
        requireNull(object, session, "value");
    }

    public static void requireNull(Object object, NutsSession session, String name) {
        if (object != null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("%s must be null", createName(name)));
        }
    }

    public static void requireNull(Object object, NutsSession session, Supplier<NutsMessage> message) {
        if (object != null) {
            throw new NutsIllegalArgumentException(session, createMessage(message, session));
        }
    }

    public static void requireNonBlank(Object object, NutsSession session, String name) {
        if (NutsBlankable.isBlank(object)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("%s should not be blank", createName(name)));
        }
    }

    public static void requireNonBlank(Object object, NutsSession session, Supplier<NutsMessage> msg) {
        if (NutsBlankable.isBlank(object)) {
            throw new NutsIllegalArgumentException(session, createMessage(msg, session));
        }
    }
}

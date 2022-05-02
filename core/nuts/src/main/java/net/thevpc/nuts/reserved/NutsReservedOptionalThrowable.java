package net.thevpc.nuts.reserved;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class NutsReservedOptionalThrowable<T> extends NutsReservedOptionalImpl<T> {
    private static boolean DEBUG;

    static {
        String property = System.getProperty("nuts.optional.debug");
        DEBUG = (property == null || property.trim().isEmpty() || Boolean.parseBoolean(property));
    }

    private Throwable rootStack = DEBUG ? new Throwable() : null;

    public NutsReservedOptionalThrowable() {
    }

    protected NutsMessage prepareMessage(NutsMessage m) {
        if (DEBUG) {
            return NutsMessage.cstyle("%s.\n    call stack:\n%s\n    root stack:\n%s", m,
                    NutsReservedLangUtils.stacktrace(new Throwable()),
                    NutsReservedLangUtils.stacktrace(rootStack)
            );
        }
        return m;
    }
}

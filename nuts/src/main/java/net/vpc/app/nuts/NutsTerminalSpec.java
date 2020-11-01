package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.Map;

public interface NutsTerminalSpec extends Serializable {
    NutsTerminalBase getParent();

    NutsTerminalSpec setParent(NutsTerminalBase parent);

    Boolean getAutoComplete();

    NutsTerminalSpec setAutoComplete(Boolean autoComplete);

    Object get(String name);

    NutsTerminalSpec put(String name, Object o);

    NutsTerminalSpec copyFrom(NutsTerminalSpec other);

    NutsTerminalSpec putAll(Map<String, Object> other);

    Map<String, Object> getProperties();

    NutsSession getSession();

    NutsTerminalSpec setSession(NutsSession session);
}

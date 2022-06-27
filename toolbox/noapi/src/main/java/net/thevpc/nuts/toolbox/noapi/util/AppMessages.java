package net.thevpc.nuts.toolbox.noapi.util;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementEntry;
import net.thevpc.nuts.elem.NutsElements;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AppMessages {
    private Map<String, String> values = new HashMap<>();
    private AppMessages parent;

    public AppMessages(AppMessages parent, URL is, NutsSession session) {
        this.parent = parent;
        NutsElement e = NutsElements.of(session).json().parse(is);
        for (NutsElementEntry entry : e.asObject().get().entries()) {
            values.put(entry.getKey().asString().get(), entry.getValue().asString().get());
        }
    }


    public NutsOptional<String> get(String key) {
        String value = values.get(key);
        if (value == null) {
            if(parent != null) {
                return parent.get(key);
            }
            return NutsOptional.ofError(s -> NutsMessage.ofCstyle("key not found : %s", key));
        }
        return NutsOptional.of(value);
    }
}

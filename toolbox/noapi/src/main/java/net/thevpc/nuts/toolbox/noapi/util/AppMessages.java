package net.thevpc.nuts.toolbox.noapi.util;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementEntry;
import net.thevpc.nuts.elem.NElements;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AppMessages {
    private Map<String, String> values = new HashMap<>();
    private AppMessages parent;

    public AppMessages(AppMessages parent, URL is, NSession session) {
        this.parent = parent;
        NElement e = NElements.of(session).json().parse(is);
        for (NElementEntry entry : e.asObject().get().entries()) {
            values.put(entry.getKey().asString().get(), entry.getValue().asString().get());
        }
    }


    public NOptional<String> get(String key) {
        String value = values.get(key);
        if (value == null) {
            if(parent != null) {
                return parent.get(key);
            }
            return NOptional.ofError(s -> NMsg.ofC("key not found : %s", key));
        }
        return NOptional.of(value);
    }
}

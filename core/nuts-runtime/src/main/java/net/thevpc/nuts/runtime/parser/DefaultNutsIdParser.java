package net.thevpc.nuts.runtime.parser;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsIdParser;
import net.thevpc.nuts.NutsParseException;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;

public class DefaultNutsIdParser implements NutsIdParser {
    private NutsWorkspace ws;
    private boolean lenient=true;

    public DefaultNutsIdParser(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsIdParser setLenient(boolean lenient) {
        this.lenient =lenient;
        return this;
    }

    @Override
    public boolean isLenient() {
        return lenient;
    }

    @Override
    public NutsId parse(String nutFormat) {
        NutsId id = CoreNutsUtils.parseNutsId(nutFormat);
        if (id == null && !isLenient()) {
            throw new NutsParseException(ws, "Invalid Id format : " + nutFormat);
        }
        return id;
    }

}

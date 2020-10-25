package net.vpc.app.nuts.runtime.parser;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;

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

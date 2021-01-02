package net.thevpc.nuts.runtime.core.parser;

import net.thevpc.nuts.NutsParseException;
import net.thevpc.nuts.NutsVersion;
import net.thevpc.nuts.NutsVersionParser;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.model.DefaultNutsVersion;

public class DefaultNutsVersionParser implements NutsVersionParser {
    private NutsWorkspace ws;
    private boolean lenient=true;

    public DefaultNutsVersionParser(NutsWorkspace ws) {
        this.ws = ws;
    }

    public NutsWorkspace getWs() {
        return ws;
    }

    @Override
    public boolean isLenient() {
        return lenient;
    }

    @Override
    public DefaultNutsVersionParser setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    @Override
    public NutsVersion parse(String version) {
        NutsVersion v = DefaultNutsVersion.valueOf(version);
        if(v==null && !isLenient()){
            throw new NutsParseException(ws, "Invalid version format : " + version);
        }
        return v;
    }
}

package net.vpc.app.nuts.runtime.parser;

import net.vpc.app.nuts.NutsParseException;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsVersionParser;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.runtime.DefaultNutsVersion;

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

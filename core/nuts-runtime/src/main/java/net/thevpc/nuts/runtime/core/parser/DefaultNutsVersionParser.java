package net.thevpc.nuts.runtime.core.parser;

import net.thevpc.nuts.NutsParseException;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsVersion;
import net.thevpc.nuts.NutsVersionParser;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.model.DefaultNutsVersion;

public class DefaultNutsVersionParser implements NutsVersionParser {
    private NutsSession session;
    private boolean lenient=true;

    public DefaultNutsVersionParser(NutsSession session) {
        this.session = session;
    }

    public NutsWorkspace getWs() {
        return session.getWorkspace();
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
        NutsVersion v = DefaultNutsVersion.valueOf(version,session);
        if(v==null && !isLenient()){
            throw new NutsParseException(session, "Invalid version format : " + version);
        }
        return v;
    }
}

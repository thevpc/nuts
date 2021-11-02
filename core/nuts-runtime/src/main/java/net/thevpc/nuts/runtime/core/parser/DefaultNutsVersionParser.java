package net.thevpc.nuts.runtime.core.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.model.DefaultNutsVersion;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.util.regex.Pattern;

public class DefaultNutsVersionParser implements NutsVersionParser {
    /**
     * ${} added to support versions as maven place-holders
     */
    private static final Pattern PATTERN=Pattern.compile("[A-Za-z0-9._*,()\\[\\] ${}-]+");
    private NutsSession session;
    private boolean lenient=false;
    private boolean acceptBlank = true;
    private boolean acceptIntervals = true;

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
    public boolean isAcceptBlank() {
        return acceptBlank;
    }

    @Override
    public NutsVersionParser setAcceptBlank(boolean acceptBlank) {
        this.acceptBlank = acceptBlank;
        return this;
    }

    public boolean isAcceptIntervals() {
        return acceptIntervals;
    }

    public NutsVersionParser setAcceptIntervals(boolean acceptIntervals) {
        this.acceptIntervals = acceptIntervals;
        return this;
    }

    @Override
    public NutsVersion parse(String version) {
        if(NutsBlankable.isBlank(version)){
            if(isAcceptBlank()){
                return new DefaultNutsVersion("",session);
            }
            throw new NutsParseException(session, NutsMessage.plain("blank version"));
        }
        String version2 = NutsUtilStrings.trim(version);
        if(PATTERN.matcher(version2).matches()) {
            DefaultNutsVersion v = new DefaultNutsVersion(version2, session);
            if(!isAcceptIntervals()){
                if(v.isFilter()){
                    throw new NutsParseException(session, NutsMessage.cstyle("invalid version format : %s", version));
                }
            }
            return v;
        }
        throw new NutsParseException(session, NutsMessage.cstyle("invalid version format : %s", version));
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> context) {
        return DEFAULT_SUPPORT;
    }
}

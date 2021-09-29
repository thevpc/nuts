package net.thevpc.nuts.runtime.core.parser;

import net.thevpc.nuts.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultNutsIdParser implements NutsIdParser {
    //(([a-zA-Z0-9_${}*-]+|<main>)://)?
    public static final Pattern NUTS_ID_PATTERN = Pattern.compile("^(?<group>[a-zA-Z0-9_.${}*-]+)(:(?<artifact>[a-zA-Z0-9_.${}*-]+))?(#(?<version>[^?]+))?(\\?(?<query>.+))?$");
    private NutsSession session;
    private boolean lenient = false;
    private boolean acceptBlank = true;

    public DefaultNutsIdParser(NutsSession session) {
        this.session = session;
    }

    public boolean isAcceptBlank() {
        return acceptBlank;
    }

    public NutsIdParser setAcceptBlank(boolean acceptBlank) {
        this.acceptBlank = acceptBlank;
        return this;
    }

    @Override
    public NutsIdParser setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    @Override
    public boolean isLenient() {
        return lenient;
    }
    /**
     * examples : script://groupId:artifactId/version?face
     * script://groupId:artifactId/version script://groupId:artifactId
     * script://artifactId artifactId
     *
     * @param nutsId nutsId
     * @return nutsId
     */
    @Override
    public NutsId parse(String nutsId) {
        if(NutsBlankable.isBlank(nutsId)){
            if(isAcceptBlank()){
                return null;
            }
            throw new NutsParseException(session, NutsMessage.plain("blank id"));
        }
        Matcher m = NUTS_ID_PATTERN.matcher(nutsId);
        if (m.find()) {
            NutsIdBuilder builder = session.id().builder();
            String group = m.group("group");
            String artifact = m.group("artifact");
            builder.setArtifactId(artifact);
            builder.setVersion(m.group("version"));
            builder.setProperties(m.group("query"));
            if (artifact == null) {
                artifact = group;
                group = null;
            }
            builder.setArtifactId(artifact);
            builder.setGroupId(group);
            return builder.build();
        }
        if (!isLenient()) {
            throw new NutsParseException(session, NutsMessage.cstyle("invalid id format : %s", nutsId));
        }
        return null;
    }

}

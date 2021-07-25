package net.thevpc.nuts.runtime.core.parser;

import net.thevpc.nuts.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultNutsIdParser implements NutsIdParser {
    public static final Pattern NUTS_ID_PATTERN = Pattern.compile("^(([a-zA-Z0-9_${}*-]+|<main>)://)?([a-zA-Z0-9_.${}*-]+)(:([a-zA-Z0-9_.${}*-]+))?(#(?<version>[^?]+))?(\\?(?<query>.+))?$");
    private NutsSession session;
    private boolean lenient = true;

    public DefaultNutsIdParser(NutsSession session) {
        this.session = session;
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
        if (nutsId != null) {
            Matcher m = NUTS_ID_PATTERN.matcher(nutsId);
            if (m.find()) {
                NutsIdBuilder builder = session.getWorkspace().id().builder();
                builder.setNamespace(m.group(2));
                String group = m.group(3);
                String artifact = m.group(5);
                builder.setArtifactId(artifact);
                builder.setVersion(m.group(7));
                builder.setProperties(m.group(9));
                if (artifact == null) {
                    artifact = group;
                    group = null;
                }
                builder.setArtifactId(artifact);
                builder.setGroupId(group);
                return builder.build();
            }
        }
        if (!isLenient()) {
            throw new NutsParseException(session, NutsMessage.cstyle("invalid id format : %s", nutsId));
        }
        return null;
    }

}

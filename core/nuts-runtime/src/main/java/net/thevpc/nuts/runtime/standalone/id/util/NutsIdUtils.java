package net.thevpc.nuts.runtime.standalone.id.util;

import net.thevpc.nuts.*;

import java.util.Map;

public class NutsIdUtils {

    public static void checkValidEffectiveId(NutsId id,NutsSession session) {
        if(id==null){
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to evaluate effective null id"));
        }
        if(id.toString().contains("${")){
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to evaluate effective id %s",id));
        }
    }

    public static NutsId createContentFaceId(NutsId id, NutsDescriptor desc, NutsSession session) {
        Map<String, String> q = id.getProperties();
        q.put(NutsConstants.IdProperties.PACKAGING, NutsUtilStrings.trim(desc.getPackaging()));
        q.put(NutsConstants.IdProperties.FACE, NutsConstants.QueryFaces.CONTENT);
        return id.builder().setProperties(q).build();
    }

    public static String getPath(NutsId id, String ext, char sep) {
        StringBuilder sb = new StringBuilder();
        sb.append(id.getGroupId().replace('.', sep));
        sb.append(sep);
        sb.append(id.getArtifactId());
        sb.append(sep);
        sb.append(id.getVersion().toString());
        sb.append(sep);
        String name = id.getArtifactId() + "-" + id.getVersion().getValue();
        sb.append(name);
        sb.append(ext);
        return sb.toString();
    }
}

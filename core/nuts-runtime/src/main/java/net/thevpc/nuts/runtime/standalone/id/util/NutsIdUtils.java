package net.thevpc.nuts.runtime.standalone.id.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsWorkspaceConfigApi;

import java.util.Map;

public class NutsIdUtils {

    public static void checkValidEffectiveId(NutsId id, NutsSession session) {
        if (id == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to evaluate effective null id"));
        }
        if (id.toString().contains("${")) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to evaluate effective id %s", id));
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

    public static boolean isApiId(NutsId id) {
        return "net.thevpc.nuts:nuts".equals(id.getShortName());
    }

    public static boolean isRuntimeId(NutsId id) {
        return "net.thevpc.nuts:nuts-runtime".equals(id.getShortName());
    }

    public static NutsId apiId(String apiVersion, NutsSession session) {
        if (NutsBlankable.isBlank(apiVersion)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid version"));
        }
        if (apiVersion.equals(session.getWorkspace().getApiVersion().toString())) {
            return session.getWorkspace().getApiId();
        }
        return NutsId.of("net.thevpc.nuts:nuts#" + apiVersion, session);
    }

    public static NutsId runtimeId(String runtimeVersion, NutsSession session) {
        if (NutsBlankable.isBlank(runtimeVersion)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid version"));
        }
        if (runtimeVersion.equals(session.getWorkspace().getApiVersion().toString())) {
            return session.getWorkspace().getApiId();
        }
        return NutsId.of("net.thevpc.nuts:nuts-runtime#" + runtimeVersion, session);
    }

    public static NutsId findRuntimeForApi(String apiVersion, NutsSession session) {
        if (NutsBlankable.isBlank(apiVersion)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid version"));
        }
        if (apiVersion.equals(session.getWorkspace().getApiVersion().toString())) {
            return session.getWorkspace().getRuntimeId();
        }
        NutsPath apiBoot = session.locations().getStoreLocation(apiId(apiVersion, session), NutsStoreLocation.CONFIG).resolve(NutsConstants.Files.API_BOOT_CONFIG_FILE_NAME);
        if (apiBoot.isRegularFile()) {
            NutsWorkspaceConfigApi c = NutsElements.of(session)
                    .setSession(session)
                    .json().parse(apiBoot, NutsWorkspaceConfigApi.class);
            if (!NutsBlankable.isBlank(c.getRuntimeId())) {
                return NutsId.of(c.getRuntimeId(), session);
            }
        }
        NutsId foundRT = session.search().addId("net.thevpc.nuts:nuts-runtime")
                .setLatest(true)
                .setTargetApiVersion(NutsVersion.of(apiVersion, session))
                .setSession(session.copy().setFetchStrategy(NutsFetchStrategy.OFFLINE)).getResultIds().first();
        if (foundRT == null && session.getFetchStrategy() != NutsFetchStrategy.OFFLINE) {
            foundRT = session.search().addId("net.thevpc.nuts:nuts-runtime")
                    .setLatest(true)
                    .setTargetApiVersion(NutsVersion.of(apiVersion, session))
                    .setSession(session).getResultIds().first();
        }
        return foundRT;
    }
}


package net.thevpc.nuts.runtime.standalone.id.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsWorkspaceConfigApi;
import net.thevpc.nuts.util.NutsStringUtils;

import java.util.Map;

public class NutsIdUtils {
    public static void checkLongId(NutsId id, NutsSession session) {
        checkShortId(id, session);
        if (NutsBlankable.isBlank(id.getVersion().toString())) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing version for %s", id));
        }
    }

    public static void checkShortId(NutsId id, NutsSession session) {
        if (id == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing id"));
        }
        if (NutsBlankable.isBlank(id.getGroupId())) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing groupId for %s", id));
        }
        if (NutsBlankable.isBlank(id.getArtifactId())) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing artifactId for %s", id));
        }
    }

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
        q.put(NutsConstants.IdProperties.PACKAGING, NutsStringUtils.trim(desc.getPackaging()));
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
        return NutsId.ofApi("").get().equalsShortId(id);
    }

    public static boolean isRuntimeId(NutsId id) {
        return NutsId.ofRuntime("").get().equalsShortId(id);
    }

    public static NutsId apiId(String apiVersion, NutsSession session) {
        if (NutsBlankable.isBlank(apiVersion)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid version"));
        }
        if (apiVersion.equals(session.getWorkspace().getApiVersion().toString())) {
            return session.getWorkspace().getApiId();
        }
        return NutsId.ofApi(apiVersion).get(session);
    }

    public static NutsId runtimeId(String runtimeVersion, NutsSession session) {
        if (NutsBlankable.isBlank(runtimeVersion)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid version"));
        }
        if (runtimeVersion.equals(session.getWorkspace().getApiVersion().toString())) {
            return session.getWorkspace().getApiId();
        }
        return NutsId.ofRuntime(runtimeVersion).get(session);
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
                return c.getRuntimeId();
            }
        }
        NutsId foundRT = session.search().addId(NutsId.ofRuntime("").get())
                .setLatest(true)
                .setTargetApiVersion(NutsVersion.of(apiVersion).get(session))
                .setSession(session.copy().setFetchStrategy(NutsFetchStrategy.OFFLINE)).getResultIds().first();
        if (foundRT == null && session.getFetchStrategy() != NutsFetchStrategy.OFFLINE) {
            foundRT = session.search().addId(NutsId.ofRuntime("").get())
                    .setLatest(true)
                    .setTargetApiVersion(NutsVersion.of(apiVersion).get(session))
                    .setSession(session).getResultIds().first();
        }
        return foundRT;
    }

    public static void checkNutsIdBase(NutsId id, NutsSession session) {
        if (id == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing id"));
        }
        if (NutsBlankable.isBlank(id.getGroupId())) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing group for %s", id));
        }
        if (NutsBlankable.isBlank(id.getArtifactId())) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing name for %s", id));
        }
    }

    public static void checkNutsId(NutsId id, NutsSession session) {
        NutsIdUtils.checkNutsIdBase(id, session);
        if (id.getVersion().isBlank()) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing version for %s", id));
        }
    }

    public static String getNutsApiVersion(NutsExecutionContext executionContext) {
        NutsSession session = executionContext.getSession();
        NutsDescriptor descriptor = executionContext.getDefinition().getDescriptor();
        if (descriptor.isApplication()) {
            for (NutsDependency dependency : descriptor.getDependencies()) {
                if (dependency.toId().getShortName().equals(NutsConstants.Ids.NUTS_API)) {
                    return dependency.toId().getVersion().getValue();
                }
            }
        }
        for (NutsDependency dependency : executionContext.getDefinition().getDependencies().get(session)) {
            if (dependency.toId().getShortName().equals(NutsConstants.Ids.NUTS_API)) {
                return dependency.toId().getVersion().getValue();
            }
        }
        return null;
    }
}


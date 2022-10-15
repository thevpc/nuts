package net.thevpc.nuts.runtime.standalone.id.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NutsDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsWorkspaceConfigApi;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.util.NutsLoggerVerb;
import net.thevpc.nuts.util.NutsStringUtils;
import net.thevpc.nuts.util.NutsUtils;

import java.util.Map;
import java.util.logging.Level;

public class NutsIdUtils {
    public static void checkLongId(NutsId id, NutsSession session) {
        checkShortId(id, session);
        NutsUtils.requireNonBlank(id.getVersion(), () -> NutsMessage.ofCstyle("missing version for %s", id), session);
    }

    public static void checkShortId(NutsId id, NutsSession session) {
        NutsUtils.requireNonBlank(id, "id", session);
        NutsUtils.requireNonBlank(id.getGroupId(), () -> NutsMessage.ofCstyle("missing groupId for %s", id), session);
        NutsUtils.requireNonBlank(id.getArtifactId(), () -> NutsMessage.ofCstyle("missing artifactId for %s", id), session);
    }

    public static boolean isValidEffectiveId(NutsId id) {
        if (NutsBlankable.isBlank(id)) {
            return false;
        }
        if (id.toString().contains("${")) {
            return false;
        }
        return true;
    }

    public static void checkValidEffectiveId(NutsId id, NutsSession session) {
        NutsUtils.requireNonBlank(id, "id", session);
        if (id.toString().contains("${")) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("unable to evaluate effective id %s", id));
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
        NutsUtils.requireNonBlank(apiVersion, "version", session);
        if (apiVersion.equals(session.getWorkspace().getApiVersion().toString())) {
            return session.getWorkspace().getApiId();
        }
        return NutsId.ofApi(apiVersion).get(session);
    }

    public static NutsId runtimeId(String runtimeVersion, NutsSession session) {
        NutsUtils.requireNonBlank(runtimeVersion, "runtimeVersion", session);
        if (runtimeVersion.equals(session.getWorkspace().getApiVersion().toString())) {
            return session.getWorkspace().getApiId();
        }
        return NutsId.ofRuntime(runtimeVersion).get(session);
    }

    public static NutsId findRuntimeForApi(String apiVersion, NutsSession session) {
        NutsUtils.requireNonBlank(apiVersion, "apiVersion", session);
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


package net.thevpc.nuts.runtime.standalone.id.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceConfigApi;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.util.List;
import java.util.Map;

public class CoreNIdUtils {
    public static NId resolveOrGenerateIdFromFileName(NPath path, NSession session) {
        List<NId> nIds = NId.findByPath(path,session);
        if (nIds.size() == 1) {
            return nIds.get(0);
        }
        if (nIds.isEmpty()) {
            return CoreNIdUtils.generateIdFromFileName(path, session);
        }
        //multiple ids ...
        return CoreNIdUtils.generateIdFromFileName(path, session);
    }

    public static NId generateIdFromFileName(NPath path, NSession session) {
        NDigest nDigest = NDigest.of(session);
        String id0 = CoreNIdUtils.resolveValidIdStringFromFileName(path.getName(), session);
        nDigest.setSource(path);
        return NId.of("temp.url:" + id0 + "-" + nDigest.computeString() + "#1.0").get();
    }

    public static String resolveValidIdStringFromFileName(String fileName, NSession session) {
        int i0 = fileName.indexOf('.');
        String base="";
        if(i0>=0){
            int i1 = fileName.lastIndexOf('.');
            if(i1>i0){
                //multiple dots
                base=fileName.substring(0,i1);
            }else if(i0==0){
                base=fileName.substring(i0+1);
            }else{
                base=fileName.substring(0,i0);
            }
        }
        StringBuilder sb=new StringBuilder();
        for (char c : base.toCharArray()) {
            if(
                    (c>='0' && c<='9')
                    || (c>='a' && c<='z')
                    || (c>='A' && c<='Z')
                    || c=='-'
                    || c=='_'
            ){
                sb.append(c);
            }else if(c=='.'){
                sb.append('-');
            }else{
                //just ignore
            }
        }
        return sb.toString();
    }

    public static void checkLongId(NId id, NSession session) {
        checkShortId(id, session);
        NAssert.requireNonBlank(id.getVersion(), () -> NMsg.ofC("missing version for %s", id), session);
    }

    public static void checkShortId(NId id, NSession session) {
        NAssert.requireNonBlank(id, "id", session);
        NAssert.requireNonBlank(id.getGroupId(), () -> NMsg.ofC("missing groupId for %s", id), session);
        NAssert.requireNonBlank(id.getArtifactId(), () -> NMsg.ofC("missing artifactId for %s", id), session);
    }

    public static boolean isValidEffectiveId(NId id) {
        if (NBlankable.isBlank(id)) {
            return false;
        }
        if (id.toString().contains("${")) {
            return false;
        }
        return true;
    }

    public static void checkValidEffectiveId(NId id, NSession session) {
        NAssert.requireNonBlank(id, "id", session);
        if (id.toString().contains("${")) {
            throw new NIllegalArgumentException(session, NMsg.ofC("unable to evaluate effective id %s", id));
        }
    }

    public static NId createContentFaceId(NId id, NDescriptor desc, NSession session) {
        Map<String, String> q = id.getProperties();
        q.put(NConstants.IdProperties.PACKAGING, NStringUtils.trim(desc.getPackaging()));
        q.put(NConstants.IdProperties.FACE, NConstants.QueryFaces.CONTENT);
        return id.builder().setProperties(q).build();
    }

    public static boolean isApiId(NId id) {
        return NId.ofApi("").get().equalsShortId(id);
    }

    public static boolean isRuntimeId(NId id) {
        return NId.ofRuntime("").get().equalsShortId(id);
    }

    public static NId apiId(String apiVersion, NSession session) {
        NAssert.requireNonBlank(apiVersion, "version", session);
        if (apiVersion.equals(session.getWorkspace().getApiVersion().toString())) {
            return session.getWorkspace().getApiId();
        }
        return NId.ofApi(apiVersion).get(session);
    }

    public static NId runtimeId(String runtimeVersion, NSession session) {
        NAssert.requireNonBlank(runtimeVersion, "runtimeVersion", session);
        if (runtimeVersion.equals(session.getWorkspace().getApiVersion().toString())) {
            return session.getWorkspace().getApiId();
        }
        return NId.ofRuntime(runtimeVersion).get(session);
    }

    public static NId findRuntimeForApi(String apiVersion, NSession session) {
        NAssert.requireNonBlank(apiVersion, "apiVersion", session);
        if (apiVersion.equals(session.getWorkspace().getApiVersion().toString())) {
            return session.getWorkspace().getRuntimeId();
        }
        NPath apiBoot = NLocations.of(session).getStoreLocation(apiId(apiVersion, session), NStoreType.CONF).resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME);
        if (apiBoot.isRegularFile()) {
            NWorkspaceConfigApi c = NElements.of(session)
                    .setSession(session)
                    .json().parse(apiBoot, NWorkspaceConfigApi.class);
            if (!NBlankable.isBlank(c.getRuntimeId())) {
                return c.getRuntimeId();
            }
        }
        NId foundRT = NSearchCmd.of(session.copy().setFetchStrategy(NFetchStrategy.OFFLINE)).addId(NId.ofRuntime("").get())
                .setLatest(true)
                .setTargetApiVersion(NVersion.of(apiVersion).get(session))
                .getResultIds().
                findFirst().orNull();
        if (foundRT == null && session.getFetchStrategy().orDefault() != NFetchStrategy.OFFLINE) {
            foundRT = NSearchCmd.of(session).addId(NId.ofRuntime("").get())
                    .setLatest(true)
                    .setTargetApiVersion(NVersion.of(apiVersion).get(session))
                    .getResultIds().
                    findFirst().orNull();
        }
        return foundRT;
    }



    public static String getNutsApiVersion(NExecutionContext executionContext) {
        NSession session = executionContext.getSession();
        NDescriptor descriptor = executionContext.getDefinition().getDescriptor();
        if (descriptor.isApplication()) {
            for (NDependency dependency : descriptor.getDependencies()) {
                if (dependency.toId().getShortName().equals(NConstants.Ids.NUTS_API)) {
                    return dependency.toId().getVersion().getValue();
                }
            }
        }
        for (NDependency dependency : executionContext.getDefinition().getDependencies().get(session)) {
            if (dependency.toId().getShortName().equals(NConstants.Ids.NUTS_API)) {
                return dependency.toId().getVersion().getValue();
            }
        }
        return null;
    }
}


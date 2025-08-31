package net.thevpc.nuts.runtime.standalone.id.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.elem.NElements;


import net.thevpc.nuts.NStoreType;
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
    public static NId resolveOrGenerateIdFromFileName(NPath path) {
        List<NId> nIds = NId.findByPath(path);
        if (nIds.size() == 1) {
            return nIds.get(0);
        }
        if (nIds.isEmpty()) {
            return CoreNIdUtils.generateIdFromFileName(path);
        }
        //multiple ids ...
        return CoreNIdUtils.generateIdFromFileName(path);
    }

    public static NId generateIdFromFileName(NPath path) {
        NDigest nDigest = NDigest.of();
        String id0 = CoreNIdUtils.resolveValidIdStringFromFileName(path.getName());
        nDigest.setSource(path);
        return NId.get("temp.url:" + id0 + "-" + nDigest.computeString() + "#1.0").get();
    }

    public static String resolveValidIdStringFromFileName(String fileName) {
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

    public static void checkLongId(NId id) {
        checkShortId(id);
        NAssert.requireNonBlank(id.getVersion(), () -> NMsg.ofC("missing version for %s", id));
    }

    public static void checkShortId(NId id) {
        NAssert.requireNonBlank(id, "id");
        NAssert.requireNonBlank(id.getGroupId(), () -> NMsg.ofC("missing groupId for %s", id));
        NAssert.requireNonBlank(id.getArtifactId(), () -> NMsg.ofC("missing artifactId for %s", id));
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

    public static void checkValidEffectiveId(NId id) {
        NAssert.requireNonBlank(id, "id");
        if (id.toString().contains("${")) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to evaluate effective id %s", id));
        }
    }

    public static NId createContentFaceId(NId id, NDescriptor desc) {
        Map<String, String> q = id.getProperties();
        q.put(NConstants.IdProperties.PACKAGING, NStringUtils.trim(desc.getPackaging()));
        q.put(NConstants.IdProperties.FACE, NConstants.QueryFaces.CONTENT);
        return id.builder().setProperties(q).build();
    }

    public static boolean isApiId(NId id) {
        return NId.getApi("").get().equalsShortId(id);
    }

    public static boolean isRuntimeId(NId id) {
        return NId.getRuntime("").get().equalsShortId(id);
    }

    public static NId apiId(String apiVersion) {
        NAssert.requireNonBlank(apiVersion, "version");
        NWorkspace workspace = NWorkspace.of();
        if (apiVersion.equals(workspace.getApiVersion().toString())) {
            return workspace.getApiId();
        }
        return NId.getApi(apiVersion).get();
    }

    public static NId runtimeId(String runtimeVersion) {
        NAssert.requireNonBlank(runtimeVersion, "runtimeVersion");
        NWorkspace workspace = NWorkspace.of();
        if (runtimeVersion.equals(workspace.getApiVersion().toString())) {
            return workspace.getApiId();
        }
        return NId.getRuntime(runtimeVersion).get();
    }

    public static NId findRuntimeForApi(String apiVersion) {
        NAssert.requireNonBlank(apiVersion, "apiVersion");
        NWorkspace workspace = NWorkspace.of();
        if (apiVersion.equals(workspace.getApiVersion().toString())) {
            return workspace.getRuntimeId();
        }
        NPath apiBoot = NPath.ofIdStore(apiId(apiVersion), NStoreType.CONF).resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME);
        if (apiBoot.isRegularFile()) {
            NWorkspaceConfigApi c = NElementParser.ofJson().parse(apiBoot, NWorkspaceConfigApi.class);
            if (!NBlankable.isBlank(c.getRuntimeId())) {
                return c.getRuntimeId();
            }
        }
        NId foundRT = NSearchCmd.of()
                .setFetchStrategy(NFetchStrategy.OFFLINE)
                .addId(NId.getRuntime("").get())
                .setLatest(true)
                .setTargetApiVersion(NVersion.get(apiVersion).get())
                .getResultIds().
                findFirst().orNull();
        NSession session = workspace.currentSession();
        if (foundRT == null && session.getFetchStrategy().orDefault() != NFetchStrategy.OFFLINE) {
            foundRT = NSearchCmd.of().addId(NId.getRuntime("").get())
                    .setLatest(true)
                    .setTargetApiVersion(NVersion.get(apiVersion).get())
                    .getResultIds().
                    findFirst().orNull();
        }
        return foundRT;
    }



    public static String getNutsApiVersion(NExecutionContext executionContext) {
        NDescriptor descriptor = executionContext.getDefinition().getDescriptor();
        if (descriptor.isNutsApplication()) {
            for (NDependency dependency : descriptor.getDependencies()) {
                if (dependency.toId().getShortName().equals(NConstants.Ids.NUTS_API)) {
                    return dependency.toId().getVersion().getValue();
                }
            }
        }
        for (NDependency dependency : executionContext.getDefinition().getDependencies().get()) {
            if (dependency.toId().getShortName().equals(NConstants.Ids.NUTS_API)) {
                return dependency.toId().getVersion().getValue();
            }
        }
        return null;
    }
}


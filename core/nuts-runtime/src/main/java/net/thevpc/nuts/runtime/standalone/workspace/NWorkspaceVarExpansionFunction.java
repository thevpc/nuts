package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NTexts;

import java.util.function.Function;

public class NWorkspaceVarExpansionFunction implements Function<String, String> {
    private final NSession session;

    public static NWorkspaceVarExpansionFunction of(NSession session) {
        return session.getOrComputeRefProperty(NWorkspaceVarExpansionFunction.class.getName(), NWorkspaceVarExpansionFunction::new);
    }

    public NWorkspaceVarExpansionFunction(NSession session) {
        this.session = session;
    }

    private String str(NPath p) {
        return p == null ? null : p.toString();
    }

    @Override
    public String apply(String from) {
        switch (from) {
            case "home.config":
                return str(session.locations().getHomeLocation(NStoreLocation.CONFIG));
            case "home.apps":
                return str(session.locations().getHomeLocation(NStoreLocation.APPS));
            case "home.lib":
                return str(session.locations().getHomeLocation(NStoreLocation.LIB));
            case "home.temp":
                return str(session.locations().getHomeLocation(NStoreLocation.TEMP));
            case "home.var":
                return str(session.locations().getHomeLocation(NStoreLocation.VAR));
            case "home.cache":
                return str(session.locations().getHomeLocation(NStoreLocation.CACHE));
            case "home.log":
                return str(session.locations().getHomeLocation(NStoreLocation.LOG));
            case "home.run":
                return str(session.locations().getHomeLocation(NStoreLocation.RUN));
            case "workspace.hash-name":
                return session.getWorkspace().getHashName();
            case "workspace.name":
                return session.getWorkspace().getName();
            case "workspace.location":
            case "workspace":
                return session.locations().getWorkspaceLocation().toString();
            case "user.home":
                return System.getProperty("user.home");
            case "workspace.config":
                return session.locations().getStoreLocation(NStoreLocation.CONFIG).toString();
            case "workspace.lib":
                return session.locations().getStoreLocation(NStoreLocation.LIB).toString();
            case "workspace.apps":
                return session.locations().getStoreLocation(NStoreLocation.APPS).toString();
            case "workspace.cache":
                return session.locations().getStoreLocation(NStoreLocation.CACHE).toString();
            case "workspace.run":
                return session.locations().getStoreLocation(NStoreLocation.RUN).toString();
            case "workspace.temp":
                return session.locations().getStoreLocation(NStoreLocation.TEMP).toString();
            case "workspace.log":
                return session.locations().getStoreLocation(NStoreLocation.LOG).toString();
            case "workspace.var":
                return session.locations().getStoreLocation(NStoreLocation.VAR).toString();
            case "nuts.boot.version":
                return session.getWorkspace().getApiVersion().toString();
            case "nuts.boot.id":
            case "nuts.api.id":
                return session.getWorkspace().getApiId().toString();
            case "nuts.api.version":
                return session.getWorkspace().getApiId().getVersion().toString();
            case "nuts.runtime.id":
                return session.getWorkspace().getRuntimeId().toString();
            case "nuts.runtime.artifact":
            case "nuts.runtime.artifactId":
                return session.getWorkspace().getRuntimeId().getArtifactId();
            case "nuts.runtime.version":
                return session.getWorkspace().getRuntimeId().getVersion().toString();
            case "nuts.workspace-boot.version":
                return Nuts.getVersion().toString();
            case "nuts.workspace-boot.id":
                return NId.ofApi(Nuts.getVersion()).get().toString();
            case "nuts.workspace-runtime.version": {
                String rt = session.boot().getBootOptions().getRuntimeId().map(Object::toString).orNull();
                return rt == null ? session.getWorkspace().getRuntimeId().getVersion().toString() : rt.contains("#")
                        ? rt.substring(rt.indexOf("#") + 1)
                        : rt;
            }
            case "nuts.workspace-runtime.id": {
                String rt = session.boot().getBootOptions().getRuntimeId().map(Object::toString).orNull();
                return rt == null ? session.getWorkspace().getRuntimeId().getVersion().toString() : rt.contains("#")
                        ? rt
                        : (NConstants.Ids.NUTS_RUNTIME + "#" + rt);
            }
            default: {
                Object v = session.info().getPropertyValue(from);
                if (v != null) {
                    return NTexts.of(session).ofText(v).filteredText();
                }
            }
        }
        String v = System.getProperty(from);
        if (v != null) {
            return v;
        }
        return "${" + from + "}";
    }
}

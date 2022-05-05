package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.text.NutsText;
import net.thevpc.nuts.text.NutsTexts;

import java.util.Map;
import java.util.function.Function;

public class NutsWorkspaceVarExpansionFunction implements Function<String, String> {
    private final NutsSession session;

    public static NutsWorkspaceVarExpansionFunction of(NutsSession session) {
        return session.getOrComputeRefProperty(NutsWorkspaceVarExpansionFunction.class.getName(), NutsWorkspaceVarExpansionFunction::new);
    }

    public NutsWorkspaceVarExpansionFunction(NutsSession session) {
        this.session = session;
    }

    private String str(NutsPath p) {
        return p == null ? null : p.toString();
    }

    @Override
    public String apply(String from) {
        switch (from) {
            case "home.config":
                return str(session.locations().getHomeLocation(NutsStoreLocation.CONFIG));
            case "home.apps":
                return str(session.locations().getHomeLocation(NutsStoreLocation.APPS));
            case "home.lib":
                return str(session.locations().getHomeLocation(NutsStoreLocation.LIB));
            case "home.temp":
                return str(session.locations().getHomeLocation(NutsStoreLocation.TEMP));
            case "home.var":
                return str(session.locations().getHomeLocation(NutsStoreLocation.VAR));
            case "home.cache":
                return str(session.locations().getHomeLocation(NutsStoreLocation.CACHE));
            case "home.log":
                return str(session.locations().getHomeLocation(NutsStoreLocation.LOG));
            case "home.run":
                return str(session.locations().getHomeLocation(NutsStoreLocation.RUN));
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
                return session.locations().getStoreLocation(NutsStoreLocation.CONFIG).toString();
            case "workspace.lib":
                return session.locations().getStoreLocation(NutsStoreLocation.LIB).toString();
            case "workspace.apps":
                return session.locations().getStoreLocation(NutsStoreLocation.APPS).toString();
            case "workspace.cache":
                return session.locations().getStoreLocation(NutsStoreLocation.CACHE).toString();
            case "workspace.run":
                return session.locations().getStoreLocation(NutsStoreLocation.RUN).toString();
            case "workspace.temp":
                return session.locations().getStoreLocation(NutsStoreLocation.TEMP).toString();
            case "workspace.log":
                return session.locations().getStoreLocation(NutsStoreLocation.LOG).toString();
            case "workspace.var":
                return session.locations().getStoreLocation(NutsStoreLocation.VAR).toString();
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
                return NutsId.ofApi(Nuts.getVersion()).get().toString();
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
                        : (NutsConstants.Ids.NUTS_RUNTIME + "#" + rt);
            }
            default: {
                Object v = session.info().getPropertyValue(from);
                if (v != null) {
                    return NutsTexts.of(session).ofText(v).filteredText();
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

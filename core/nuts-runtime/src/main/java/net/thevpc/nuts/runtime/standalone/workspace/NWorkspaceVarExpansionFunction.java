package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;



import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NText;

import java.util.function.Function;

public class NWorkspaceVarExpansionFunction implements Function<String, String> {
    private final NWorkspace workspace;

    public static NWorkspaceVarExpansionFunction of() {
        NWorkspace ws = NWorkspace.of().get();
        return ws.currentSession().getOrComputeProperty(NWorkspaceVarExpansionFunction.class.getName(), NScopeType.SESSION, ()->new NWorkspaceVarExpansionFunction(ws));
    }

    public NWorkspaceVarExpansionFunction(NWorkspace workspace) {
        this.workspace = workspace;
    }

    private String str(NPath p) {
        return p == null ? null : p.toString();
    }

    @Override
    public String apply(String from) {
        switch (from) {
            case "home.conf":
                return str(NWorkspace.get().getHomeLocation(NStoreType.CONF));
            case "home.bin":
                return str(NWorkspace.get().getHomeLocation(NStoreType.BIN));
            case "home.lib":
                return str(NWorkspace.get().getHomeLocation(NStoreType.LIB));
            case "home.temp":
                return str(NWorkspace.get().getHomeLocation(NStoreType.TEMP));
            case "home.var":
                return str(NWorkspace.get().getHomeLocation(NStoreType.VAR));
            case "home.cache":
                return str(NWorkspace.get().getHomeLocation(NStoreType.CACHE));
            case "home.log":
                return str(NWorkspace.get().getHomeLocation(NStoreType.LOG));
            case "home.run":
                return str(NWorkspace.get().getHomeLocation(NStoreType.RUN));
            case "workspace.hash-name":
                return workspace.getHashName();
            case "workspace.name":
                return workspace.getName();
            case "workspace.location":
            case "workspace":
                return NWorkspace.get().getWorkspaceLocation().toString();
            case "user.home":
                return System.getProperty("user.home");
            case "workspace.config":
                return NWorkspace.get().getStoreLocation(NStoreType.CONF).toString();
            case "workspace.lib":
                return NWorkspace.get().getStoreLocation(NStoreType.LIB).toString();
            case "workspace.apps":
                return NWorkspace.get().getStoreLocation(NStoreType.BIN).toString();
            case "workspace.cache":
                return NWorkspace.get().getStoreLocation(NStoreType.CACHE).toString();
            case "workspace.run":
                return NWorkspace.get().getStoreLocation(NStoreType.RUN).toString();
            case "workspace.temp":
                return NWorkspace.get().getStoreLocation(NStoreType.TEMP).toString();
            case "workspace.log":
                return NWorkspace.get().getStoreLocation(NStoreType.LOG).toString();
            case "workspace.var":
                return NWorkspace.get().getStoreLocation(NStoreType.VAR).toString();
            case "nuts.boot.version":
                return workspace.getApiVersion().toString();
            case "nuts.boot.id":
            case "nuts.api.id":
                return workspace.getApiId().toString();
            case "nuts.api.version":
                return workspace.getApiId().getVersion().toString();
            case "nuts.runtime.id":
                return workspace.getRuntimeId().toString();
            case "nuts.runtime.artifact":
            case "nuts.runtime.artifactId":
                return workspace.getRuntimeId().getArtifactId();
            case "nuts.runtime.version":
                return workspace.getRuntimeId().getVersion().toString();
            case "nuts.workspace-boot.version":
                return Nuts.getVersion().toString();
            case "nuts.workspace-boot.id":
                return NId.ofApi(Nuts.getVersion()).get().toString();
            case "nuts.workspace-runtime.version": {
                String rt = NWorkspace.get().getBootOptions().getRuntimeId().map(Object::toString).orNull();
                return rt == null ? workspace.getRuntimeId().getVersion().toString() : rt.contains("#")
                        ? rt.substring(rt.indexOf("#") + 1)
                        : rt;
            }
            case "nuts.workspace-runtime.id": {
                String rt = NWorkspace.get().getBootOptions().getRuntimeId().map(Object::toString).orNull();
                return rt == null ? workspace.getRuntimeId().getVersion().toString() : rt.contains("#")
                        ? rt
                        : (NConstants.Ids.NUTS_RUNTIME + "#" + rt);
            }
            default: {
                Object v = NInfoCmd.of().getPropertyValue(from).orNull();
                if (v != null) {
                    return NText.of(v).filteredText();
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

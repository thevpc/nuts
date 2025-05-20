package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;



import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NText;

import java.util.function.Function;

public class NWorkspaceVarExpansionFunction implements Function<String, String> {
    private final NWorkspace workspace;

    public static NWorkspaceVarExpansionFunction of() {
        NWorkspace ws = NWorkspace.get().get();
        return NApp.of().getOrComputeProperty(NWorkspaceVarExpansionFunction.class.getName(), NScopeType.SESSION, ()->new NWorkspaceVarExpansionFunction(ws));
    }

    public NWorkspaceVarExpansionFunction(NWorkspace workspace) {
        this.workspace = workspace;
    }

    private String str(Object p) {
        return p == null ? null : p.toString();
    }

    @Override
    public String apply(String from) {
        switch (from) {
            case "home.conf":
                return str(NWorkspace.of().getHomeLocation(NStoreType.CONF));
            case "home.bin":
                return str(NWorkspace.of().getHomeLocation(NStoreType.BIN));
            case "home.lib":
                return str(NWorkspace.of().getHomeLocation(NStoreType.LIB));
            case "home.temp":
                return str(NWorkspace.of().getHomeLocation(NStoreType.TEMP));
            case "home.var":
                return str(NWorkspace.of().getHomeLocation(NStoreType.VAR));
            case "home.cache":
                return str(NWorkspace.of().getHomeLocation(NStoreType.CACHE));
            case "home.log":
                return str(NWorkspace.of().getHomeLocation(NStoreType.LOG));
            case "home.run":
                return str(NWorkspace.of().getHomeLocation(NStoreType.RUN));
            case "workspace.hash-name":
                return workspace.getDigestName();
            case "workspace.name":
                return workspace.getName();
            case "workspace.location":
            case "workspace":
                return str(NWorkspace.of().getWorkspaceLocation());
            case "user.home":
                return System.getProperty("user.home");
            case "workspace.config":
                return str(NWorkspace.of().getStoreLocation(NStoreType.CONF));
            case "workspace.lib":
                return str(NWorkspace.of().getStoreLocation(NStoreType.LIB));
            case "workspace.apps":
                return str(NWorkspace.of().getStoreLocation(NStoreType.BIN));
            case "workspace.cache":
                return str(NWorkspace.of().getStoreLocation(NStoreType.CACHE));
            case "workspace.run":
                return str(NWorkspace.of().getStoreLocation(NStoreType.RUN));
            case "workspace.temp":
                return str(NWorkspace.of().getStoreLocation(NStoreType.TEMP));
            case "workspace.log":
                return str(NWorkspace.of().getStoreLocation(NStoreType.LOG));
            case "workspace.var":
                return str(NWorkspace.of().getStoreLocation(NStoreType.VAR));
            case "nuts.boot.version":
                return str(workspace.getApiVersion());
            case "nuts.boot.id":
            case "nuts.api.id":
                return str(workspace.getApiId());
            case "nuts.api.version":
                return workspace.getApiId().getVersion().toString();
            case "nuts.runtime.id":
                return str(workspace.getRuntimeId());
            case "nuts.runtime.artifact":
            case "nuts.runtime.artifactId":
                return str(workspace.getRuntimeId());
            case "nuts.runtime.version":
                return str(workspace.getRuntimeId().getVersion());
            case "nuts.workspace-boot.version":
                return str(Nuts.getVersion());
            case "nuts.workspace-boot.id":
                return str(NId.getApi(Nuts.getVersion()).orNull());
            case "nuts.workspace-runtime.version": {
                String rt = NWorkspace.of().getBootOptions().getRuntimeId().map(this::str).orNull();
                return rt == null ? str(workspace.getRuntimeId().getVersion()) : rt.contains("#")
                        ? rt.substring(rt.indexOf("#") + 1)
                        : rt;
            }
            case "nuts.workspace-runtime.id": {
                String rt = NWorkspace.of().getBootOptions().getRuntimeId().map(this::str).orNull();
                return rt == null ? str(workspace.getRuntimeId().getVersion()) : rt.contains("#")
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

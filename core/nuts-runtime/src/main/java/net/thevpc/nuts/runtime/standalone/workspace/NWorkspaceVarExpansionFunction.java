package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.NConstants;


import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NInfoCmd;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NStoreKey;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.platform.NStoreScope;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NText;

import java.util.function.Function;

public class NWorkspaceVarExpansionFunction implements Function<String, String> {
    private static NWorkspaceVarExpansionFunction INSTANCE=new NWorkspaceVarExpansionFunction();
    public static NWorkspaceVarExpansionFunction of() {
        return INSTANCE;
    }

    public NWorkspaceVarExpansionFunction() {
    }

    private String str(Object p) {
        return p == null ? null : p.toString();
    }

    @Override
    public String apply(String from) {
        switch (from) {
            case "home.conf":
                return str(NPath.of(NStoreKey.ofBase(NStoreType.CONF)));
            case "home.bin":
                return str(NPath.of(NStoreKey.ofBase(NStoreType.BIN)));
            case "home.lib":
                return str(NPath.of(NStoreKey.ofBase(NStoreType.LIB)));
            case "home.temp":
                return str(NPath.of(NStoreKey.ofBase(NStoreType.TEMP)));
            case "home.var":
                return str(NPath.of(NStoreKey.ofBase(NStoreType.VAR)));
            case "home.cache":
                return str(NPath.of(NStoreKey.ofBase(NStoreType.CACHE)));
            case "home.log":
                return str(NPath.of(NStoreKey.ofBase(NStoreType.LOG)));
            case "home.run":
                return str(NPath.of(NStoreKey.ofBase(NStoreType.RUN)));
            case "workspace.hash-name":
                return NWorkspace.of().getDigestName();
            case "workspace.name":
                return NWorkspace.of().getName();
            case "workspace.location":
            case "workspace":
                return str(NWorkspace.of().getWorkspaceLocation());
            case "user.home":
                return System.getProperty("user.home");
            case "workspace.config":
                return str(NPath.of(NStoreKey.ofConf()));
            case "workspace.lib":
                return str(NPath.of(NStoreKey.ofLib()));
            case "workspace.apps":
                return str(NPath.of(NStoreKey.ofBin()));
            case "workspace.cache":
                return str(NPath.of(NStoreKey.ofCache()));
            case "workspace.run":
                return str(NPath.of(NStoreKey.ofRun()));
            case "workspace.temp":
                return str(NPath.of(NStoreKey.ofTemp()));
            case "workspace.log":
                return str(NPath.of(NStoreKey.ofLog()));
            case "workspace.var":
                return str(NPath.of(NStoreKey.ofVar()));
            case "nuts.boot.version":
                return str(NWorkspace.of().getApiVersion());
            case "nuts.boot.id":
            case "nuts.api.id":
                return str(NWorkspace.of().getApiId());
            case "nuts.api.version":
                return NWorkspace.of().getApiId().getVersion().toString();
            case "nuts.runtime.id":
                return str(NWorkspace.of().getRuntimeId());
            case "nuts.runtime.artifact":
            case "nuts.runtime.artifactId":
                return str(NWorkspace.of().getRuntimeId());
            case "nuts.runtime.version":
                return str(NWorkspace.of().getRuntimeId().getVersion());
            case "nuts.workspace-boot.version":
                return str(Nuts.getVersion());
            case "nuts.workspace-boot.id":
                return str(NId.getApi(Nuts.getVersion()).orNull());
            case "nuts.workspace-runtime.version": {
                String rt = NWorkspace.of().getBootOptions().getRuntimeId().map(this::str).orNull();
                return rt == null ? str(NWorkspace.of().getRuntimeId().getVersion()) : rt.contains("#")
                        ? rt.substring(rt.indexOf("#") + 1)
                        : rt;
            }
            case "nuts.workspace-runtime.id": {
                String rt = NWorkspace.of().getBootOptions().getRuntimeId().map(this::str).orNull();
                return rt == null ? str(NWorkspace.of().getRuntimeId().getVersion()) : rt.contains("#")
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

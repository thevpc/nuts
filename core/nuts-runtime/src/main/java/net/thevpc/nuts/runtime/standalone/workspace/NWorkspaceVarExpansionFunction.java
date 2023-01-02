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
                return str(NLocations.of(session).getHomeLocation(NStoreLocation.CONFIG));
            case "home.apps":
                return str(NLocations.of(session).getHomeLocation(NStoreLocation.APPS));
            case "home.lib":
                return str(NLocations.of(session).getHomeLocation(NStoreLocation.LIB));
            case "home.temp":
                return str(NLocations.of(session).getHomeLocation(NStoreLocation.TEMP));
            case "home.var":
                return str(NLocations.of(session).getHomeLocation(NStoreLocation.VAR));
            case "home.cache":
                return str(NLocations.of(session).getHomeLocation(NStoreLocation.CACHE));
            case "home.log":
                return str(NLocations.of(session).getHomeLocation(NStoreLocation.LOG));
            case "home.run":
                return str(NLocations.of(session).getHomeLocation(NStoreLocation.RUN));
            case "workspace.hash-name":
                return session.getWorkspace().getHashName();
            case "workspace.name":
                return session.getWorkspace().getName();
            case "workspace.location":
            case "workspace":
                return NLocations.of(session).getWorkspaceLocation().toString();
            case "user.home":
                return System.getProperty("user.home");
            case "workspace.config":
                return NLocations.of(session).getStoreLocation(NStoreLocation.CONFIG).toString();
            case "workspace.lib":
                return NLocations.of(session).getStoreLocation(NStoreLocation.LIB).toString();
            case "workspace.apps":
                return NLocations.of(session).getStoreLocation(NStoreLocation.APPS).toString();
            case "workspace.cache":
                return NLocations.of(session).getStoreLocation(NStoreLocation.CACHE).toString();
            case "workspace.run":
                return NLocations.of(session).getStoreLocation(NStoreLocation.RUN).toString();
            case "workspace.temp":
                return NLocations.of(session).getStoreLocation(NStoreLocation.TEMP).toString();
            case "workspace.log":
                return NLocations.of(session).getStoreLocation(NStoreLocation.LOG).toString();
            case "workspace.var":
                return NLocations.of(session).getStoreLocation(NStoreLocation.VAR).toString();
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
                String rt = NBootManager.of(session).getBootOptions().getRuntimeId().map(Object::toString).orNull();
                return rt == null ? session.getWorkspace().getRuntimeId().getVersion().toString() : rt.contains("#")
                        ? rt.substring(rt.indexOf("#") + 1)
                        : rt;
            }
            case "nuts.workspace-runtime.id": {
                String rt = NBootManager.of(session).getBootOptions().getRuntimeId().map(Object::toString).orNull();
                return rt == null ? session.getWorkspace().getRuntimeId().getVersion().toString() : rt.contains("#")
                        ? rt
                        : (NConstants.Ids.NUTS_RUNTIME + "#" + rt);
            }
            default: {
                Object v = NInfoCommand.of(session).getPropertyValue(from);
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

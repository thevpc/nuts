package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsStoreLocation;
import net.thevpc.nuts.NutsWorkspace;

import java.util.function.Function;

public class NutsWorkspaceVarExpansionFunction implements Function<String, String> {
    private final NutsWorkspace ws;

    public NutsWorkspaceVarExpansionFunction(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String apply(String from) {
        switch (from) {
            case "home.config":
                return ws.locations().getHomeLocation(NutsStoreLocation.CONFIG);
            case "home.apps":
                return ws.locations().getHomeLocation(NutsStoreLocation.APPS);
            case "home.lib":
                return ws.locations().getHomeLocation(NutsStoreLocation.LIB);
            case "home.temp":
                return ws.locations().getHomeLocation(NutsStoreLocation.TEMP);
            case "home.var":
                return ws.locations().getHomeLocation(NutsStoreLocation.VAR);
            case "home.cache":
                return ws.locations().getHomeLocation(NutsStoreLocation.CACHE);
            case "home.log":
                return ws.locations().getHomeLocation(NutsStoreLocation.LOG);
            case "home.run":
                return ws.locations().getHomeLocation(NutsStoreLocation.RUN);
            case "workspace":
                return ws.locations().getWorkspaceLocation().toString();
            case "user.home":
                return System.getProperty("user.home");
            case "config":
                return ws.locations().getStoreLocation(NutsStoreLocation.CONFIG).toString();
            case "lib":
                return ws.locations().getStoreLocation(NutsStoreLocation.LIB).toString();
            case "apps":
                return ws.locations().getStoreLocation(NutsStoreLocation.APPS).toString();
            case "cache":
                return ws.locations().getStoreLocation(NutsStoreLocation.CACHE).toString();
            case "run":
                return ws.locations().getStoreLocation(NutsStoreLocation.RUN).toString();
            case "temp":
                return ws.locations().getStoreLocation(NutsStoreLocation.TEMP).toString();
            case "log":
                return ws.locations().getStoreLocation(NutsStoreLocation.LOG).toString();
            case "var":
                return ws.locations().getStoreLocation(NutsStoreLocation.VAR).toString();
            case "nuts.boot.version":
                return ws.getApiVersion();
            case "nuts.boot.id":
                return ws.getApiId().toString();
            case "nuts.workspace-boot.version":
                return Nuts.getVersion();
            case "nuts.workspace-boot.id":
                return NutsConstants.Ids.NUTS_API + "#" + Nuts.getVersion();
            case "nuts.workspace-runtime.version": {
                String rt = ws.config().getOptions().getRuntimeId();
                return rt == null ? ws.getRuntimeId().getVersion().toString() : rt.contains("#")
                        ? rt.substring(rt.indexOf("#") + 1)
                        : rt;
            }
            case "nuts.workspace-runtime.id": {
                String rt = ws.config().getOptions().getRuntimeId();
                return rt == null ? ws.getRuntimeId().getVersion().toString() : rt.contains("#")
                        ? rt
                        : (NutsConstants.Ids.NUTS_RUNTIME + "#" + rt);
            }
        }
        String v = System.getProperty(from);
        if (v != null) {
            return v;
        }
        return "${" + from + "}";
    }
}

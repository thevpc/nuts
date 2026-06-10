package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NExecTargetSPI;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NOptional;

public class NExtensionUtils {
    public static void ensureExtensionLoadedForProtocol(NConnectionString connectionString) {
        if (NBlankable.isBlank(connectionString)) {
            return ;//
        }
        ensureExtensionLoadedForProtocol(connectionString.protocol());
    }

    public static void ensureExtensionLoadedForProtocol(String protocol) {
        if (NBlankable.isBlank(protocol)) {
            return ;//
        }
        NWorkspaceExt.of().getModel().extensionCatalogManager.loadExtensionFor("net.thevpc.nuts.spi.path", protocol);
    }

    public static NExecTargetSPI createNExecTargetSPI(NConnectionString connectionString) {
        if (!NBlankable.isBlank(connectionString)) {
            ensureExtensionLoadedForProtocol(connectionString);
            return NExtensions.of().createSupported(NExecTargetSPI.class, connectionString)
                    .orElseThrow(() -> new NIllegalArgumentException(NMsg.ofC("invalid execution target string : %s", connectionString)));
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid execution target string : %s", connectionString));
    }

    public static boolean isBootstrapLogType(Class apiType) {
        switch (apiType.getName()) {
            //skip logging this to avoid infinite recursion
            case "net.thevpc.nuts.io.NPaths":
            case "net.thevpc.nuts.text.NTexts":
            case "net.thevpc.nuts.log.NLogs":
            case "net.thevpc.nuts.log.NLog": {
                return true;
            }
        }
        return false;
    }

}

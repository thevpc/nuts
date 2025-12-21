package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NExecTargetSPI;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NOptional;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class NExtensionUtils {
    private static Map<String, NId> protocolToExtensionMap = new HashMap<>(
            NMaps.of(
                    "ssh", NId.of("net.thevpc.nuts:nuts-ssh"),
                    "nagent", NId.of("com.cts.nuts.enterprise:next-agent")
            )
    );

    public static NOptional<NId> ensureExtensionLoadedForProtocol(NConnectionString connectionString) {
        if (NBlankable.isBlank(connectionString) || NBlankable.isBlank(connectionString.getProtocol())) {
            return NOptional.ofNamedEmpty("protocol");
        }
        return ensureExtensionLoadedForProtocol(connectionString == null ? null : connectionString.getProtocol());
    }

    public static NOptional<NId> ensureExtensionLoadedForProtocol(String protocol) {
        NOptional<NId> u = extensionForProtocol(protocol);
        if (u.isPresent()) {
            NExtensions.of().loadExtension(u.get());
        }
        return u;
    }

    public static NOptional<NId> extensionForProtocol(String protocol) {
        return NOptional.ofNamed(protocolToExtensionMap.get(protocol), protocol);
    }


    public static NExecTargetSPI createNExecTargetSPI(NConnectionString connectionString) {
        if (!NBlankable.isBlank(connectionString)) {
            NExtensionUtils.ensureExtensionLoadedForProtocol(connectionString);
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

    public static void safeLog(NMsg msg, Throwable any) {
        //TODO: should we use boot stdio?
        PrintStream err = NWorkspaceExt.of().getModel().bootModel.getBootTerminal().getErr();
        if (err == null) {
            err = System.err;
        }
        err.println(msg.toString() + ":");
        any.printStackTrace();
    }




}

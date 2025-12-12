package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.spi.NExecTargetSPI;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NOptional;

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
            return NExtensions.of().createComponent(NExecTargetSPI.class, connectionString)
                    .orElseThrow(() -> new NIllegalArgumentException(NMsg.ofC("invalid execution target string : %s", connectionString)));
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid execution target string : %s", connectionString));
    }
}

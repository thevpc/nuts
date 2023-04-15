package net.thevpc.nuts.lib.ssh;

import net.thevpc.nuts.NExecCommandExtension;
import net.thevpc.nuts.NExecCommandExtensionContext;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NConnexionString;

public class SshNExecCommandExtension implements NExecCommandExtension {
    @Override
    public int exec(NExecCommandExtensionContext context) {
        String host = context.getHost();
        NAssert.requireNonBlank(host, "host");
        NConnexionString z = NConnexionString.of(host).orNull();
        NAssert.requireNonBlank(z, "host");
        try (SShConnection c = new SShConnection(host, context.getSession())) {
            String[] command = context.getCommand();
            return c.execStringCommand(String.join(" ",command));
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        Object c = context.getConstraints();
        if (c instanceof String) {
            NConnexionString z = NConnexionString.of((String) c).orNull();
            if (z != null && "ssh".equals(z.getProtocol())) {
                return DEFAULT_SUPPORT;
            }
        }
        if (c instanceof NConnexionString) {
            NConnexionString z = (NConnexionString) c;
            if ("ssh".equals(z.getProtocol())) {
                return DEFAULT_SUPPORT;
            }
        }
        return NO_SUPPORT;
    }
}

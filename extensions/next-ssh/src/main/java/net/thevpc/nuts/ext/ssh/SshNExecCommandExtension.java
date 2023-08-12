package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NConnexionString;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.util.NLogVerb;

import java.io.*;
import java.util.logging.Level;

public class SshNExecCommandExtension implements NExecCommandExtension {

    @Override
    public int exec(NExecCommandExtensionContext context) {
        String host = context.getHost();
        NAssert.requireNonBlank(host, "host");
        NConnexionString z = NConnexionString.of(host).orNull();
        NAssert.requireNonBlank(z, "host");
        NSession session = context.getSession();
        NLog log = NLog.of(SshNExecCommandExtension.class, session);
        log.with().level(Level.FINER).verb(NLogVerb.START).log(NMsg.ofC("[%s] %s", z, NCmdLine.of(context.getCommand(), session)));
        try (SShConnection c = new SShConnection(
                host,
                context.in(),
                context.out(),
                context.err(),
                session)) {
            String[] command = context.getCommand();
            return c.execStringCommand(NCmdLine.of(command).toString());
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        Object c = context.getConstraints();
        if (c instanceof String) {
            NConnexionString z = NConnexionString.of((String) c).orNull();
            if (z != null && "ssh".equals(z.getProtocol())) {
                return NConstants.Support.DEFAULT_SUPPORT;
            }
        }
        if (c instanceof NConnexionString) {
            NConnexionString z = (NConnexionString) c;
            if ("ssh".equals(z.getProtocol())) {
                return NConstants.Support.DEFAULT_SUPPORT;
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }

}

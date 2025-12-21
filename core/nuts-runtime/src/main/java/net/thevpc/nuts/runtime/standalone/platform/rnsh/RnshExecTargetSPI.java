package net.thevpc.nuts.runtime.standalone.platform.rnsh;

import net.thevpc.nuts.net.DefaultNConnectionStringBuilder;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.spi.NExecTargetCommandContext;
import net.thevpc.nuts.spi.NExecTargetSPI;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.*;

public class RnshExecTargetSPI implements NExecTargetSPI {

    @Override
    public int exec(NExecTargetCommandContext context) {
        return  RnshPool.of().get(context.getConnectionString()).exec(context.getCommand(), context.isRawCommand(), context.in(), context.out(), context.err());
    }

    @NScore
    public static int getScore(NScorableContext context) {
        Object c = context.getCriteria();

        if (c instanceof String) {
            NConnectionStringBuilder z = DefaultNConnectionStringBuilder.of((String) c).orNull();
            if (z != null && isSupportedProtocol(z.getProtocol())) {
                return NScorable.DEFAULT_SCORE;
            }
        }
        if (c instanceof NConnectionStringBuilder) {
            NConnectionStringBuilder z = (NConnectionStringBuilder) c;
            if (isSupportedProtocol(z.getProtocol())) {
                return NScorable.DEFAULT_SCORE;
            }
        }
        if (c instanceof NConnectionString) {
            NConnectionString z = (NConnectionString) c;
            if (isSupportedProtocol(z.getProtocol())) {
                return NScorable.DEFAULT_SCORE;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    private static boolean isSupportedProtocol(String protocol) {
        return ("rnsh".equals(protocol)
                || "rnsh-http".equals(protocol)
                || "rnsh-https".equals(protocol)
                || "rnshs".equals(protocol)
        );
    }
}

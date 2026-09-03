package net.thevpc.nuts.runtime.standalone.platform.rnsh;

import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.runtime.standalone.net.DefaultNConnectionStringBuilder;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.spi.NExecTargetCommandContext;
import net.thevpc.nuts.spi.NExecTargetSPI;
import net.thevpc.nuts.reflect.NScorableContext;

public class RnshExecTargetSPI implements NExecTargetSPI {

    @Override
    public int exec(NExecTargetCommandContext context) {
        String dir = context.execCommand() != null && context.execCommand().directory() != null ? context.execCommand().directory().toString() : null;
        boolean hasDir = !net.thevpc.nuts.util.NBlankable.isBlank(dir);
        String[] cmd = context.command();
        if (hasDir) {
            if (context.isRawCommand()) {
                cmd = new String[]{"cd " + quoteArg(dir) + " && " + cmd[0]};
            } else {
                cmd = new String[]{"cd " + quoteArg(dir) + " && " + cmdArrayToString(cmd)};
            }
        }
        return RnshPool.of().get(context.connectionString()).exec(cmd, context.isRawCommand(), context.in(), context.out(), context.err());
    }

    private static String cmdArrayToString(String[] command) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < command.length; i++) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(quoteArg(command[i]));
        }
        return sb.toString();
    }

    private static String quoteArg(String arg) {
        if (arg == null || arg.isEmpty()) {
            return "\"\"";
        }
        return "'" + arg.replace("'", "'\\''") + "'";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        Object c = context.criteria();

        if (c instanceof String) {
            NConnectionStringBuilder z = DefaultNConnectionStringBuilder.of((String) c).orNull();
            if (z != null && isSupportedProtocol(z.protocol())) {
                return NScorable.DEFAULT_SCORE;
            }
        }
        if (c instanceof NConnectionStringBuilder) {
            NConnectionStringBuilder z = (NConnectionStringBuilder) c;
            if (isSupportedProtocol(z.protocol())) {
                return NScorable.DEFAULT_SCORE;
            }
        }
        if (c instanceof NConnectionString) {
            NConnectionString z = (NConnectionString) c;
            if (isSupportedProtocol(z.protocol())) {
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

package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.spi.base.NEnvAsCmdBase;

public class NEnvSshImpl extends NEnvAsCmdBase {
    public static final String PROTOCOL = "ssh";

    public NEnvSshImpl(NScorableContext context) {
        super(context, PROTOCOL);
    }

    public NEnvSshImpl(NConnectionString connectionString) {
        super(connectionString, PROTOCOL);
    }

    @Override
    public NEnv refresh() {
        return new NEnvSshImpl(connectionString());
    }

    @NScore
    public static int getScore(NScorableContext context) {
        Object c = context.criteria();
        if (c instanceof NConnectionString) {
            NConnectionString z = (NConnectionString) c;
            if (PROTOCOL.equals(z.protocol())) {
                return NScorable.DEFAULT_SCORE;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    protected String runSystemCommand(String cmd) {
        try (SshConnection sshc = SshConnectionPool.of().acquire(connectionString())) {
            return sshc.execStringCommandGrabbed(cmd).outString();
        }
    }

}

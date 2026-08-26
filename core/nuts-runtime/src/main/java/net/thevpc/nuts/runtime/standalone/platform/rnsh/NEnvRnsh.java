package net.thevpc.nuts.runtime.standalone.platform.rnsh;

import net.thevpc.nuts.io.NullInputStream;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.spi.base.NEnvAsCmdBase;
import net.thevpc.nuts.util.*;

import java.io.ByteArrayOutputStream;

public class NEnvRnsh extends NEnvAsCmdBase {
    public static final String PROTOCOL = "rnsh";

    public NEnvRnsh(NScorableContext context) {
        super(context, PROTOCOL);
    }

    public NEnvRnsh(NConnectionString connectionString) {
        super(connectionString, PROTOCOL);
    }

    @Override
    public NEnv refresh() {
        return new NEnvRnsh(connectionString());
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
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int x = RnshPool.of().get(connectionString()).exec(new String[]{cmd}, true, NullInputStream.INSTANCE, out, err);
        return out.toString();
    }

}

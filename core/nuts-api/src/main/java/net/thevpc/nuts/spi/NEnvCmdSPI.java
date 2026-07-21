package net.thevpc.nuts.spi;

import net.thevpc.nuts.net.NConnectionString;

public interface NEnvCmdSPI {
    String exec(String cmd);
    NConnectionString targetConnectionString();
}

package net.thevpc.nuts.spi;

import net.thevpc.nuts.net.NConnectionString;

/**
 * NEnvCmdSPI interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NEnvCmdSPI {
    /**
     * Exec.
     *
     * @param cmd cmd
     * @return exec result
     */
    String exec(String cmd);
    /**
     * Target connection string.
     *
     * @return target connection string result
     */
    NConnectionString targetConnectionString();
}

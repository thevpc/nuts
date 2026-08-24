package net.thevpc.nuts.spi;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.net.NConnectionString;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * NExecTargetCommandContext interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExecTargetCommandContext {
    /**
     * Connection string.
     *
     * @return connection string result
     */
    NConnectionString connectionString();

    /**
     * Command.
     *
     * @return command result
     */
    String[] command();

    /**
     * Checks if is raw command.
     *
     * @return is raw command result
     */
    boolean isRawCommand();

    /**
     * In.
     *
     * @return in result
     */
    InputStream in();

    /**
     * Out.
     *
     * @return out result
     */
    OutputStream out();

    /**
     * Err.
     *
     * @return err result
     */
    OutputStream err();

    /**
     * Exec command.
     *
     * @return exec command result
     */
    NExec execCommand();
}

package net.thevpc.nuts.spi;

import net.thevpc.nuts.net.NConnectionString;

public interface NExecTargetInfoRunner {
    /**
     * Execute a command on the execution target and return raw output.
     * Used by DefaultTargetInfo to run probe commands.
     */
    String run(String command, NConnectionString connectionString);
}

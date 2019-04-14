package net.vpc.app.nuts;

public enum NutsExecutionType {
    /**
     * command will be resolved as an external command.
     * Nuts will resolve relevant executor to run it
     */
    SPAWN,
    /**
     * command will be resolved as an external native command.
     * Nuts will delegate running to underlining operating system
     * using standard ProcessBuilder
     */
    SYSCALL,
    /**
     * command will resolved as a class to run within the current
     * Virtual Machine
     */
    EMBEDDED,
}

package net.thevpc.nuts;

/**
 * Default Logging verb names
 *
 * @category Logging
 */
public final class NutsLogVerb {

    public static final String INFO = "INFO";
    public static final String DEBUG = "DEBUG";

    /**
     * Log verb used for tracing the start of an operation
     */
    public static final String START = "START";

    /**
     * Log verb used for tracing the successful termination of an operation
     */
    public static final String SUCCESS = "SUCCESS";

    /**
     * Log verb used for tracing general purpose warnings
     */
    public static final String WARNING = "WARNING";

    /**
     * Log verb used for tracing the failure to run an operation
     */
    public static final String FAIL = "FAIL";

    /**
     * Log verb used for tracing a I/O read operation
     */
    public static final String READ = "READ";

    public static final String UPDATE = "UPDATE";

    /**
     * Log verb used for tracing cache related operations
     */
    public static final String CACHE = "CACHE";

    private NutsLogVerb() {
    }
}

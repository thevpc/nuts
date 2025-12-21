package net.thevpc.nuts.platform;

import net.thevpc.nuts.net.NConnectionString;

public interface NConnectionStringAware {
    /**
     * Returns the connection string representing the target host for execution.
     * When non-blank, this connection string will be used to connect to a remote host.
     *
     * @return the target host connection string
     * @since 0.8.9
     */
    NConnectionString getConnectionString();

    /**
     * Updates the target host connection string.
     * When non-blank, the connection string will be used to connect to a remote host.
     *
     * @param connectionString target host connection string
     * @return this instance for fluent API usage
     */
    default NConnectionStringAware setConnectionString(String connectionString) {
        throw new UnsupportedOperationException("Override in concrete interface");
    }

    /**
     * Shortcut to set the connection string for execution.
     *
     * @param connectionString target host connection string
     * @return this instance for fluent API usage
     */
    default NConnectionStringAware at(String connectionString) {
        throw new UnsupportedOperationException("Override in concrete interface");
    }

    /**
     * Shortcut to set the connection string for execution using a typed object.
     *
     * @param connectionString target host connection object
     * @return this instance for fluent API usage
     */
    default NConnectionStringAware at(NConnectionString connectionString) {
        throw new UnsupportedOperationException("Override in concrete interface");
    }

    /**
     * Sets the connection string for execution using a typed object.
     *
     * @param connectionString target host connection object
     * @return this instance for fluent API usage
     */
    default NConnectionStringAware setConnectionString(NConnectionString connectionString) {
        throw new UnsupportedOperationException("Override in concrete interface");
    }
}

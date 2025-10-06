/**
 * The {@code net.thevpc.nuts.log} package provides a unified logging framework
 * for the Nuts ecosystem.
 * <p>
 * It allows creating and configuring loggers, recording structured log messages,
 * and integrating with various underlying logging systems.
 * </p>
 *
 * <p>
 * Key features:
 * <ul>
 *     <li><b>Logger abstraction:</b> NLog and NLogs provide a consistent API
 *         for creating and using loggers across Nuts applications.</li>
 *     <li><b>Configuration:</b> NLogConfig allows configuring log levels, output
 *         formats, handlers, and filtering rules.</li>
 *     <li><b>Contextual logging:</b> NLogContext allows propagating contextual
 *         information such as session ID, user, or transaction across log records.</li>
 *     <li><b>Structured log records:</b> NLogRecord supports metadata-rich
 *         logging for better analysis and monitoring.</li>
 *     <li><b>Extensible SPI:</b> NLogSPI and NLogFactorySPI allow implementing
 *         custom logging backends or adapting existing ones.</li>
 *     <li><b>Utility helpers:</b> NLogUtils and NMsgIntent simplify common
 *         logging operations and intent categorization.</li>
 *     <li><b>Null logger:</b> NullNLog is provided for cases where logging
 *         should be disabled or ignored safely.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Typical use cases:
 * <ul>
 *     <li>Application-level logging with configurable levels and handlers.</li>
 *     <li>Structured logging for monitoring and auditing purposes.</li>
 *     <li>Propagation of contextual information through distributed or multi-threaded operations.</li>
 *     <li>Integration with custom logging frameworks or sinks.</li>
 * </ul>
 * </p>
 *
 * <p>
 * This package is designed to be lightweight, extensible, and fully compatible
 * with the Nuts runtime, ensuring consistent logging practices across all modules.
 * </p>
 *
 * @since 0.8.6
 */
package net.thevpc.nuts.log;

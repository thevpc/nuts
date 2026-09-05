/**
 * Reserved Programming Interfaces (RPIs) used to wire public API facilities to the
 * default Nuts runtime.
 *
 * <p>These interfaces are deliberately not application-facing SPIs. Public API code
 * calls them internally for services such as elements, expressions, I/O, text,
 * logging, concurrency, command lines, and artifact filters. The default runtime
 * supplies and registers their implementations. Applications and third-party
 * extensions must neither call nor implement these interfaces directly; use the
 * public API or the supported {@code net.thevpc.nuts.spi} extension points instead.</p>
 */
package net.thevpc.nuts.internal.rpi;

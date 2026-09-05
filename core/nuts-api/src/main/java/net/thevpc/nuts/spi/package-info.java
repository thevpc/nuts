/**
 * Supported Service Provider Interfaces for extending a Nuts runtime.
 *
 * <p>Providers contribute {@link net.thevpc.nuts.spi.NComponent components} for
 * repositories and repository commands, artifact descriptors and dependency solving,
 * paths and content types, terminal and logging integration, packaging, application
 * resolution, execution targets, and extension lifecycle. Implementations are
 * discovered and composed by the runtime according to component scope and metadata.</p>
 *
 * <p>This is the supported integration surface. It is distinct from
 * {@code net.thevpc.nuts.internal.rpi}, whose RPIs are reserved for API-to-runtime
 * wiring and must not be implemented by third parties.</p>
 */
package net.thevpc.nuts.spi;

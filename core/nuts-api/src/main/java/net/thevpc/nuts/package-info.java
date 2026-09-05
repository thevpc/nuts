/**
 * Public API for Nuts, a runtime and package-management platform for discovering,
 * resolving, installing, and executing software artifacts.
 *
 * <p>The API is organized by concern: artifact coordinates and descriptors,
 * sessions and workspaces, repositories and commands, I/O and terminal handling,
 * structured elements and text, and extension points. Applications normally enter
 * through the workspace/session contracts in {@code net.thevpc.nuts.core}.</p>
 *
 * <p>Provider extension points belong to {@code net.thevpc.nuts.spi}. Types under
 * {@code net.thevpc.nuts.internal}, including its RPI subpackage, are not public
 * application APIs.</p>
 */
package net.thevpc.nuts;

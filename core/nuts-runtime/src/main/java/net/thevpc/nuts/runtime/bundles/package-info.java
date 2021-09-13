/**
 * this package contains reusable statically linked libraries.
 *
 * nuts-runtime must have null to minimum dependencies and as such it should
 * whenever possible use statically linked libraries.
 * In java the only way to do statically linked "private" libraries is to copy the library and prefix them with
 * another package (here net.thevpc.nuts.runtime.bundles).
 * The Java SDK did the same with xml libs and so on.
 *
 * This package contains these statically libraries. All of these libraries have been created for
 * nuts itself (like ntalk and nanodb) and they could be exported as a standalone library while
 * bundled here.
 */
package net.thevpc.nuts.runtime.bundles;
package net.thevpc.nuts;

import net.thevpc.nuts.core.NDetachedExceptionBase;

/**
 * Detached Exceptions are Nuts Exception that
 * are not bound to any NWorkspace/NSession
 */
public interface NDetachedFormattedExceptionBase extends NAnyFormattedExceptionBase, NDetachedExceptionBase {
}

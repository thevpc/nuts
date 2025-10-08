package net.thevpc.nuts.util;

import net.thevpc.nuts.boot.core.NDetachedExceptionBase;

/**
 * Detached Exceptions are Nuts Exception that
 * are not bound to any NWorkspace/NSession
 */
public interface NDetachedFormattedExceptionBase extends NAnyFormattedExceptionBase, NDetachedExceptionBase {
}

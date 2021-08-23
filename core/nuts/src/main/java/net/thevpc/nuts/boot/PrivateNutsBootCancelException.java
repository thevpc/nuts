package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsBootException;
import net.thevpc.nuts.NutsMessage;

class PrivateNutsBootCancelException extends NutsBootException {
    public PrivateNutsBootCancelException(NutsMessage message) {
        super(message);
    }
}

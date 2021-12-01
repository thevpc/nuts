package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;

public interface NutsApplicationExceptionHandler extends NutsComponent {
    static NutsApplicationExceptionHandler of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsApplicationExceptionHandler.class, true, session);
    }
    int processThrowable(String[] args,Throwable throwable,NutsSession session);
}

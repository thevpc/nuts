package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;

import java.io.OutputStream;
import java.io.Writer;

public interface NutsPrintStreams extends NutsComponent<Object> {
    static NutsPrintStreams of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsPrintStreams.class, true, session);
    }


    NutsPrintStream createNull() ;

    NutsMemoryPrintStream createInMemory() ;

    /**
     * create print stream that supports the given {@code mode}. If the given
     * {@code out} is a PrintStream that supports {@code mode}, it should be
     * returned without modification.
     *
     * @param out  stream to wrap
     * @param mode mode to support
     * @return {@code mode} supporting PrintStream
     */
    NutsPrintStream create(OutputStream out, NutsTerminalMode mode) ;

    NutsPrintStream create(OutputStream out) ;

    NutsPrintStream create(Writer out, NutsTerminalMode mode);

    NutsPrintStream create(Writer out) ;

    boolean isStdout(NutsPrintStream out);

    boolean isStderr(NutsPrintStream out);

    NutsPrintStream stdout();

    NutsPrintStream stderr();

}

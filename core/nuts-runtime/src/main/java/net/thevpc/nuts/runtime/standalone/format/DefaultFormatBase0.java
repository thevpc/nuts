/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.NPrintStream;

/**
 *
 * @author thevpc
 */
public abstract class DefaultFormatBase0<T> implements NCmdLineConfigurable {

    private final String name;
    private boolean ntf=true;

    public DefaultFormatBase0(String name) {
        this.name = name;
    }
//    public DefaultFormatBase0(NSession session, String name) {
//        this.session = session;
//        this.workspace = session.getWorkspace();
//        this.name = name;
//    }



    public NPrintStream getValidPrintStream(NPrintStream out) {
        if (out == null) {
            NSession session=NSession.of();
            out = session.getTerminal().getOut();
        }
        return out;
    }

    public NPrintStream getValidPrintStream() {
        return getValidPrintStream(null);
    }

    public String getName() {
        return name;
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public T configure(boolean skipUnsupported, String... args) {
        return NCmdLineConfigurable.configure(this, skipUnsupported, args,getName());
    }

    public boolean isNtf() {
        return ntf;
    }

    public T setNtf(boolean ntf) {
        this.ntf = ntf;
        return (T)this;
    }

}

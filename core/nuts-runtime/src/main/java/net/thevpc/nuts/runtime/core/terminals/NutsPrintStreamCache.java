package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.NutsPrintStream;
import net.thevpc.nuts.NutsSession;

public class NutsPrintStreamCache {
    private NutsPrintStream base;
    private NutsPrintStream result;
    private NutsSession session;

    public NutsPrintStream get(NutsPrintStream base,NutsSession session){
        if(base.getSession()==session){
            return base;
        }
        if(session==this.session && base==this.base &&  result!=null){
            return result;
        }
        this.base=base;
        this.session=session;
        this.result= this.base.setSession(this.session);
        return this.result;
    }
}

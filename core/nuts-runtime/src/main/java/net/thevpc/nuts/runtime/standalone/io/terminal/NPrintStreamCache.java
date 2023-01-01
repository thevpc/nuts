package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.NSession;

public class NPrintStreamCache {
    private NOutStream base;
    private NOutStream result;
    private NSession session;

    public NOutStream get(NOutStream base, NSession session){
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

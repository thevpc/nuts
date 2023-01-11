package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.NSession;

public class NPrintStreamCache {
    private NOutputStream base;
    private NOutputStream result;
    private NSession session;

    public NOutputStream get(NOutputStream base, NSession session){
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

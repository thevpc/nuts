//package net.thevpc.nuts.runtime.standalone.io.terminal;
//
//import net.thevpc.nuts.io.NPrintStream;
//import net.thevpc.nuts.NSession;
//
//public class NPrintStreamCache {
//    private NPrintStream base;
//    private NPrintStream result;
//
//    public NPrintStream get(NPrintStream base){
//        if(base.getSession()==session){
//            return base;
//        }
//        if(session==this.session && base==this.base &&  result!=null){
//            return result;
//        }
//        this.base=base;
//        this.session=session;
//        this.result= this.base.setSession(this.session);
//        return this.result;
//    }
//}

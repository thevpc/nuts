package net.thevpc.nuts.util;

public interface NMsgCodeAware {
    static NOptional<NMsgCode> codeOf(Object any){
        if(any instanceof NMsgCodeAware){
            return NOptional.of(((NMsgCodeAware) any).getMsgCode());
        }
        return NOptional.ofEmpty();
    }

    NMsgCode getMsgCode();
}

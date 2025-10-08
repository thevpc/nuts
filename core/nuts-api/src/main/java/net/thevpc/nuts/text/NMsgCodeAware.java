package net.thevpc.nuts.text;

import net.thevpc.nuts.util.NOptional;

public interface NMsgCodeAware {
    static NOptional<NMsgCode> codeOf(Object any){
        if(any instanceof NMsgCodeAware){
            return NOptional.of(((NMsgCodeAware) any).getMsgCode());
        }
        return NOptional.ofEmpty();
    }

    NMsgCode getMsgCode();
}

package net.thevpc.nuts.util;

import net.thevpc.nuts.util.NMsgCode;
import net.thevpc.nuts.util.NOptional;

public interface NMsgCodeAware {
    static NOptional<NMsgCode> codeOf(Object any){
        if(any instanceof NMsgCodeAware){
            return NOptional.of(((NMsgCodeAware) any).getNMsgCode());
        }
        return NOptional.ofEmpty();
    }

    NMsgCode getNMsgCode();
}

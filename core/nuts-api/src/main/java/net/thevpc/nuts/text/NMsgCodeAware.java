package net.thevpc.nuts.text;

import net.thevpc.nuts.util.NOptional;

/**
 * NMsgCodeAware interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NMsgCodeAware {
    /**
     * Code of.
     *
     * @param any any
     * @return code of result
     */
    static NOptional<NMsgCode> codeOf(Object any){
        if(any instanceof NMsgCodeAware){
            return NOptional.of(((NMsgCodeAware) any).msgCode());
        }
        return NOptional.ofEmpty();
    }

    /**
     * Msg code.
     *
     * @return msg code result
     */
    NMsgCode msgCode();
}

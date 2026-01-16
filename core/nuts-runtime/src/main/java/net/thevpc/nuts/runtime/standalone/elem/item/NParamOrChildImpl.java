package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NParamOrChild;

public class NParamOrChildImpl implements NParamOrChild {
    /**
     * the actual element
     */
    private NElement element;

    /**
     * index in params if param, else -1
     */
    private int index;

    /**
     * true if this came from params
     */
    private boolean param;

    public static NParamOrChildImpl param(NElement element, int index) {
        return new NParamOrChildImpl(element, index, true);
    }

    public static NParamOrChildImpl child(NElement element, int index) {
        return new NParamOrChildImpl(element, index, false);
    }

    public NParamOrChildImpl(NElement element, int index, boolean param) {
        this.element = element;
        this.index = index;
        this.param = param;
    }

    @Override
    public NElement element() {
        return element;
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public boolean isParam() {
        return param;
    }

    @Override
    public boolean isChild() {
        return !param;
    }
}

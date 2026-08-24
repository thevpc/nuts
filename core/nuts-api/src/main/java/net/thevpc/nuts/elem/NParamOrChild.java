package net.thevpc.nuts.elem;

/**
 * NParamOrChild interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NParamOrChild {
    /** the actual element */
    NElement element();

    /** index in params if param, else -1 */
    int index();

    /** true if this came from params */
    boolean isParam();

    /** true if this came from children */
    boolean isChild();
}

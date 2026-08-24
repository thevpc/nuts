package net.thevpc.nuts.elem;

/**
 * NOperatorSymbolElement interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NOperatorSymbolElement extends NElement {
    /**
     * Symbol.
     *
     * @return symbol result
     */
    NOperatorSymbol symbol();
    /**
     * Builder.
     *
     * @return builder result
     */
    NOperatorSymbolElementBuilder builder();
}

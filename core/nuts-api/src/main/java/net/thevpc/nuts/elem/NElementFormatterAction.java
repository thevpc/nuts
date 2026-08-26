package net.thevpc.nuts.elem;

/**
 * NElementFormatterAction interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementFormatterAction {
    /**
     * Apply.
     *
     * @param context context
     */
    void apply(NElementFormatContext context);

    /**
     * Prepare child context.
     *
     * @param parent parent
     * @param childContext child context
     * @return prepare child context result
     */
    default NElementFormatContext prepareChildContext(NElement parent, NElementFormatContext childContext){
        return childContext;
    }
}

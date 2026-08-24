package net.thevpc.nuts.text;

/**
 * NTextTransformer interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NTextTransformer {
    /**
     * Pre transform.
     *
     * @param text text
     * @param context context
     * @return pre transform result
     */
    default NText preTransform(NText text, NTextTransformerContext context){
        return text;
    }

    /**
     * Post transform.
     *
     * @param text text
     * @param context context
     * @return post transform result
     */
    NText postTransform(NText text, NTextTransformerContext context);
}

package net.thevpc.nuts.text;

/**
 * NTextTransformerContext interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NTextTransformerContext {
    /**
     * Config.
     *
     * @return config result
     */
    NTextTransformConfig config();

    /**
     * Title sequence.
     *
     * @return title sequence result
     */
    NTitleSequence titleSequence();

    /**
     * Title sequence.
     *
     * @param sequence sequence
     * @return title sequence result
     */
    NTextTransformerContext titleSequence(NTitleSequence sequence);

    /**
     * Default transformer.
     *
     * @return default transformer result
     */
    NTextTransformer defaultTransformer();

    /**
     * Default transformer.
     *
     * @param transformer transformer
     * @return default transformer result
     */
    NTextTransformerContext defaultTransformer(NTextTransformer transformer);

    /**
     * Copy.
     *
     * @return copy result
     */
    NTextTransformerContext copy();
}

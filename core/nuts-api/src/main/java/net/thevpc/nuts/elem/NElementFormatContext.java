package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NContentType;

/**
 * NElementFormatContext interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementFormatContext extends NElementTransformContext {
    /**
     * Indent.
     *
     * @return indent result
     */
    String indent();

    /**
     * Content type.
     *
     * @return content type result
     */
    NContentType contentType();

    /**
     * Options.
     *
     * @return options result
     */
    NElementFormatOptions options();

    /**
     * Builder.
     *
     * @return builder result
     */
    NElementBuilder builder();

    /**
     * With path.
     *
     * @param path path
     * @return with path result
     */
    NElementFormatContext withPath(NElementPath path);

    /**
     * With element.
     *
     * @param element element
     * @return with element result
     */
    NElementFormatContext withElement(NElement element);

    /**
     * With indent.
     *
     * @param value value
     * @return with indent result
     */
    NElementFormatContext withIndent(String value);

    /**
     * With builder.
     *
     * @param builder builder
     * @return with builder result
     */
    NElementFormatContext withBuilder(NElementBuilder builder);

    /**
     * With options.
     *
     * @param options options
     * @return with options result
     */
    NElementFormatContext withOptions(NElementFormatOptions options);
}

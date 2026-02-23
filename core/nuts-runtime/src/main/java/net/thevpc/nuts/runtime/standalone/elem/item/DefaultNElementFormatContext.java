package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Map;
import java.util.Objects;

public class DefaultNElementFormatContext extends DefaultNElementTransformContext implements NElementFormatContext {
    private String indent = "";
    private NElementFormatOptions options;
    private NElementBuilder builder;
    private NContentType contentType;

    public DefaultNElementFormatContext(NElement element, NContentType contentType) {
        super(element);
        this.contentType = contentType;
    }

    public DefaultNElementFormatContext(NElement element, NElementPath path, Map<String, Object> properties, Map<String, Object> sharedConfig, boolean lastElement, String indent, NElementFormatOptions options, NElementBuilder builder) {
        super(element, path, properties, sharedConfig, lastElement);
        this.indent = NStringUtils.firstNonNull(indent, "");
        this.options = options;
        this.builder = builder;
    }

    public NContentType contentType() {
        return contentType;
    }

    @Override
    public String indent() {
        return indent;
    }

    @Override
    public NElementFormatOptions options() {
        return options;
    }

    @Override
    public NElementBuilder builder() {
        return builder;
    }

    @Override
    public NElementFormatContext withBuilder(NElementBuilder builder) {
        return newInstance(element(), path(), properties(), sharedConfig(), isTail(), indent, options, builder);
    }

    @Override
    public NElementFormatContext withOptions(NElementFormatOptions options) {
        if (Objects.equals(options, this.options)) {
            return this;
        }
        return newInstance(element(), path(), properties(), sharedConfig(), isTail(), indent, options, builder);
    }

    @Override
    public NElementFormatContext withPath(NElementPath path) {
        return (NElementFormatContext) super.withPath(path);
    }

    @Override
    public NElementFormatContext withElement(NElement element) {
        return (NElementFormatContext) super.withElement(element);
    }


    public NElementFormatContext withIndent(String indent) {
        return newInstance(element(), path(), properties(), sharedConfig(), isTail(), indent, options, builder);
    }

    @Override
    protected NElementFormatContext newInstance(NElement element, NElementPath path, Map<String, Object> properties, Map<String, Object> sharedConfig, boolean lastElement) {
        return newInstance(element, path, properties, sharedConfig, lastElement, indent, options, builder);
    }

    protected NElementFormatContext newInstance(NElement element, NElementPath path, Map<String, Object> properties, Map<String, Object> sharedConfig, boolean lastElement, String indent, NElementFormatOptions options, NElementBuilder builder) {
        return new DefaultNElementFormatContext(element, path, properties, sharedConfig, lastElement, indent, options, builder);
    }
}

package net.thevpc.nuts.elem;

public interface NElementFormatContext extends NElementTransformContext {
    String indent();

    NElementFormatOptions options();

    NElementBuilder builder();

    NElementFormatContext withPath(NElementPath path);

    NElementFormatContext withElement(NElement element);

    NElementFormatContext withIndent(String value);

    NElementFormatContext withBuilder(NElementBuilder builder);

    NElementFormatContext withOptions(NElementFormatOptions options);
}

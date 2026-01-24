package net.thevpc.nuts.elem;

public interface NElementFormatterAction {
    void apply(NElementFormatContext context);

    default NElementFormatContext prepareChildContext(NElement parent, NElementFormatContext childContext){
        return childContext;
    }
}

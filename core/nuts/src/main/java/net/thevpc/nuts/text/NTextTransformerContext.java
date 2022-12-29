package net.thevpc.nuts.text;

public interface NTextTransformerContext {
    NTitleSequence getTitleSequence();

    NTextTransformerContext setTitleSequence(NTitleSequence sequence);

    NTextTransformer getDefaultTransformer();

    NTextTransformerContext setDefaultTransformer(NTextTransformer transformer);

    NTextTransformerContext copy();
}

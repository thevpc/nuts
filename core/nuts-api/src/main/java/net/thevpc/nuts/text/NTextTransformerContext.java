package net.thevpc.nuts.text;

public interface NTextTransformerContext {
    NTextTransformConfig config();

    NTitleSequence titleSequence();

    NTextTransformerContext titleSequence(NTitleSequence sequence);

    NTextTransformer defaultTransformer();

    NTextTransformerContext defaultTransformer(NTextTransformer transformer);

    NTextTransformerContext copy();
}

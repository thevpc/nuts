package net.thevpc.nuts.text;

public interface NutsTextTransformerContext {
    NutsTitleSequence getTitleSequence();

    NutsTextTransformerContext setTitleSequence(NutsTitleSequence sequence);

    NutsTextTransformer getDefaultTransformer();

    NutsTextTransformerContext setDefaultTransformer(NutsTextTransformer transformer);

    NutsTextTransformerContext copy();
}

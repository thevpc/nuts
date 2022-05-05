package net.thevpc.nuts.text;

public interface NutsTextTransformer {
    default NutsText preTransform(NutsText text, NutsTextTransformerContext context){
        return text;
    }

    NutsText postTransform(NutsText text, NutsTextTransformerContext context);
}

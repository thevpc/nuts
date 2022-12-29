package net.thevpc.nuts.text;

public interface NTextTransformer {
    default NText preTransform(NText text, NTextTransformerContext context){
        return text;
    }

    NText postTransform(NText text, NTextTransformerContext context);
}

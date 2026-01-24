package net.thevpc.nuts.elem;

public interface NElementComment extends Comparable<NElementComment>, NAffix {

    /**
     * create some comments
     *
     * @param text comment
     * @return NElementComment
     * @since 0.8.9
     */
    static NElementComment of(String text) {
        return ofMultiLine(text);
    }

    /**
     * create some multiline comments
     *
     * @param text comment
     * @return NElementComment
     * @since 0.8.9
     */
    static NElementComment ofMultiLine(String... text) {
        return NElement.ofBlocComment(text);
    }

    /**
     * create some single comments
     *
     * @param text comment
     * @return NElementComment
     * @since 0.8.9
     */
    static NElementComment ofSingleLine(String... text) {
        return NElement.ofLineComment(text);
    }


    String text();

    String raw();
}

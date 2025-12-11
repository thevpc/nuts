package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NBlankable;

public interface NElementComment extends Comparable<NElementComment>, NBlankable {

    /**
     * create some comments
     * @param text comment
     * @return NElementComment
     * @since 0.8.9
     */
    static NElementComment of(String text) {
        return ofMultiLine(text);
    }

    /**
     * create some multiline comments
     * @param text comment
     * @return NElementComment
     * @since 0.8.9
     */
    static NElementComment ofMultiLine(String... text) {
        return NElement.ofMultiLineComment(text);
    }

    /**
     * create some single comments
     * @param text comment
     * @return NElementComment
     * @since 0.8.9
     */
    static NElementComment ofSingleLine(String... text) {
        return NElement.ofSingleLineComment(text);
    }

    NElementCommentType type();

    String text() ;
}

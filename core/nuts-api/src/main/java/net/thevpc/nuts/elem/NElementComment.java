package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NNewLineMode;

import java.util.List;

public interface NElementComment extends /*Comparable<NElementComment>, */NAffix {

    /**
     * create some comments
     *
     * @param text comment
     * @return NElementComment
     * @since 0.8.9
     */
    static NElementComment of(String text) {
        return ofBlocComment(text);
    }

    /**
     * create some multiline comments
     *
     * @param text comment
     * @return NElementComment
     * @since 0.8.9
     */
    static NElementComment ofBlocComment(String text) {
        return NElement.ofBlocComment(text);
    }

    /**
     * create some single comments
     *
     * @param text comment
     * @return NElementComment
     * @since 0.8.9
     */
    static NElementComment ofLineComment(String text) {
        return NElement.ofLineComment(text);
    }

    static NElementComment ofLineComment(NElementLine... text) {
        return NElement.ofLineComment(text);
    }

    static NElementComment ofBlocComment(NElementLine... text) {
        return NElement.ofBlocComment(text);
    }


    NNewLineMode newlineSuffix();

    List<NElementLine> lines();

    String text();

    String raw();

    NElementComment withNewlineSuffix(NNewLineMode nNewLineMode);
}

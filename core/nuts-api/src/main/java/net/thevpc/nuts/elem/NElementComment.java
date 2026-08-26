package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NNewLineMode;

import java.util.List;

/**
 * NElementComment interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementComment extends /*Comparable<NElementComment>, */NAffix {

    /**
     * create some comments
     *
     * @param text comment
     * @return NElementComment
     * @since 0.8.9
     */
    static NElementComment of(String text) {
        /**
         * Creates a new instance of of bloc comment.
         *
         * @param text text
         * @return of bloc comment result
         */
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

    /**
     * Creates a new instance of of line comment.
     *
     * @param text text
     * @return of line comment result
     */
    static NElementComment ofLineComment(NElementLine... text) {
        return NElement.ofLineComment(text);
    }

    /**
     * Creates a new instance of of bloc comment.
     *
     * @param text text
     * @return of bloc comment result
     */
    static NElementComment ofBlocComment(NElementLine... text) {
        return NElement.ofBlocComment(text);
    }


    /**
     * Newline suffix.
     *
     * @return newline suffix result
     */
    NNewLineMode newlineSuffix();

    /**
     * Lines.
     *
     * @return lines result
     */
    List<NElementLine> lines();

    /**
     * Text.
     *
     * @return text result
     */
    String text();

    /**
     * Raw.
     *
     * @return raw result
     */
    String raw();

    /**
     * With newline suffix.
     *
     * @param nNewLineMode n new line mode
     * @return with newline suffix result
     */
    NElementComment withNewlineSuffix(NNewLineMode nNewLineMode);
}

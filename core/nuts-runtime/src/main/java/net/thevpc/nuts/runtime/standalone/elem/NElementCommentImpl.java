package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.NElementComment;
import net.thevpc.nuts.elem.NElementCommentType;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringBuilder;

import java.util.Objects;

public class NElementCommentImpl implements NElementComment {
    private NElementCommentType type;
    private String text;

    public static NElementComment of(String text) {
        return ofMultiLine(text);
    }

    public static NElementCommentImpl ofMultiLine(String text) {
        return new NElementCommentImpl(NElementCommentType.MULTI_LINE, text);
    }

    public static NElementCommentImpl ofSingleLine(String text) {
        return new NElementCommentImpl(NElementCommentType.SINGLE_LINE, text);
    }

    public NElementCommentImpl(NElementCommentType type, String text) {
        this.type = type;
        this.text = text == null ? "" : text;
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(text);
    }

    /****
     * this is a comment
     ****/
    @Override
    public String toString() {
        switch (type) {
            case SINGLE_LINE: {
                return new NStringBuilder(text).indent("// ").toString();
            }
            case MULTI_LINE: {
                return "/*\n"
                        + new NStringBuilder(text).indent("// ").toString()
                        + "*/"
                        ;
            }
        }
        return new NStringBuilder(text).indent("// ").toString();
    }

    public NElementCommentType type() {
        return type;
    }

    public String text() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NElementCommentImpl that = (NElementCommentImpl) o;
        return type == that.type && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, text);
    }

    @Override
    public int compareTo(NElementComment o) {
        int i = text.compareTo(o.text());
        if (i != 0) {
            return i;
        }
        return type.compareTo(o.type());
    }
}

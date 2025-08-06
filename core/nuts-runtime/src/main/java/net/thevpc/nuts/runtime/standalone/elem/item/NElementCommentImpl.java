package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.NElementComment;
import net.thevpc.nuts.elem.NElementCommentType;
import net.thevpc.nuts.util.NStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NElementCommentImpl implements NElementComment {
    private NElementCommentType type;
    private List<String> lines = new ArrayList<>();

    public static NElementComment of(String text) {
        return ofMultiLine(text);
    }

    public static NElementCommentImpl ofMultiLine(String... text) {
        return new NElementCommentImpl(NElementCommentType.MULTI_LINE, text);
    }

    public static NElementCommentImpl ofSingleLine(String... text) {
        return new NElementCommentImpl(NElementCommentType.SINGLE_LINE, text);
    }

    public NElementCommentImpl(NElementCommentType type, String... texts) {
        this.type = type;
        if (texts != null) {
            for (String text : texts) {
                this.lines.addAll(new NStringBuilder(text).lines().toList());
            }
        }
    }

    @Override
    public boolean isBlank() {
        return lines.isEmpty();
    }

    /****
     * this is a comment
     ****/
    @Override
    public String toString() {
        switch (type) {
            case SINGLE_LINE: {
                return new NStringBuilder(text()).indent("// ").append("\n").toString();
            }
            case MULTI_LINE: {
                return "/*\n"
                        + new NStringBuilder(text()).indent("* ").toString()
                        + "*/"
                        ;
            }
        }
        return new NStringBuilder(text()).indent("// ").toString();
    }

    public NElementCommentType type() {
        return type;
    }

    public String text() {
        return lines.stream().collect(Collectors.joining("\n"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NElementCommentImpl that = (NElementCommentImpl) o;
        return type == that.type && Objects.equals(lines, that.lines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, lines);
    }

    @Override
    public int compareTo(NElementComment o) {
        int i = text().compareTo(o.text());
        if (i != 0) {
            return i;
        }
        return type.compareTo(o.type());
    }
}

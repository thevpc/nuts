package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.NElementComment;
import net.thevpc.nuts.elem.NAffixType;
import net.thevpc.nuts.runtime.standalone.elem.writer.DefaultTsonWriter;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.custom.TsonCommentsHelper;
import net.thevpc.nuts.util.NStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NElementCommentImpl implements NElementComment {
    private NAffixType type;
    private String raw;
    private List<String> lines = new ArrayList<>();
    private static NElementCommentImpl EMPTY_MULTI_LINE = new NElementCommentImpl(NAffixType.BLOC_COMMENT, "/**/", "");
    private static NElementCommentImpl EMPTY_SINGLE_LINE = new NElementCommentImpl(NAffixType.LINE_COMMENT, "//", "");

    public static NElementComment of(String text) {
        return ofBloc(text);
    }

    public static NElementCommentImpl ofBloc(String... text) {
        if (text == null || text.length == 0 || (text.length == 1 && (text[0] == null || text[0].isEmpty()))) {
            return EMPTY_MULTI_LINE;
        }
        return new NElementCommentImpl(NAffixType.BLOC_COMMENT, null, text);
    }

    public static NElementCommentImpl ofLine(String... text) {
        if (text == null || text.length == 0 || (text.length == 1 && (text[0] == null || text[0].isEmpty()))) {
            return EMPTY_SINGLE_LINE;
        }
        return new NElementCommentImpl(NAffixType.LINE_COMMENT, null, text);
    }

    public NElementCommentImpl(NAffixType type, String raw, String... texts) {
        this.type = type;
        if (type == NAffixType.LINE_COMMENT) {
            this.lines.addAll(TsonCommentsHelper.normalizeLineComment(texts));
            if (raw == null) {
                this.raw = lines.stream().map(x -> "// " + x).collect(Collectors.joining("\n"));
            } else {
                this.raw = raw;
            }
        } else {
            if (raw == null) {
                this.raw = "/*" + lines.stream().collect(Collectors.joining("\n")) + "*/";
            } else {
                this.raw = raw;
            }
        }
    }

    public static NElementComment of(NAffixType type, String text) {
        return type == NAffixType.BLOC_COMMENT ? ofBloc(text) : ofLine(text);
    }

    @Override
    public String raw() {
        return raw;
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
        return DefaultTsonWriter.formatTson(this);
    }

    public NAffixType type() {
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

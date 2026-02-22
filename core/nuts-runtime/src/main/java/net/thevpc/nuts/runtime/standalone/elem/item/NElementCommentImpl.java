package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.NElementComment;
import net.thevpc.nuts.elem.NAffixType;
import net.thevpc.nuts.elem.NElementLine;
import net.thevpc.nuts.runtime.standalone.elem.writer.DefaultTsonWriter;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementLineImpl;
import net.thevpc.nuts.text.NLine;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NIllegalArgumentException;

import java.util.*;

public class NElementCommentImpl implements NElementComment {
    private final NAffixType type;
    private final String raw;
    private final String content;
    private final List<NElementLine> lines;
    private static final NElementCommentImpl EMPTY_BLOC_COMMENT = new NElementCommentImpl(NAffixType.BLOC_COMMENT, "/**/", "", new NElementLineImpl("", "/*", "", "", "", "*/", null));
    private static final NElementCommentImpl EMPTY_LINE_COMMENT = new NElementCommentImpl(NAffixType.LINE_COMMENT, "//", "", new NElementLineImpl("", "//", "", "", "", "", null));

    public static NElementComment of(String text) {
        return ofBloc(text);
    }

    public static NElementCommentImpl ofBloc(String text) {
        if (text == null || text.isEmpty()) {
            return EMPTY_BLOC_COMMENT;
        }
        List<NLine> lines = NLine.parseList(text);
        if (lines.isEmpty()) {
            return EMPTY_BLOC_COMMENT;
        }
        if (lines.size() == 1) {
            List<NElementLineImpl> all = new ArrayList<>();
            all.add(new NElementLineImpl("", "/*", " ", lines.get(0).content(), "", " */", null));
            return new NElementCommentImpl(NAffixType.LINE_COMMENT, NElementLineImpl.concatString(all), text, all.toArray(new NElementLine[0]));
        }

        List<NElementLineImpl> all = new ArrayList<>();
        all.add(new NElementLineImpl("", "/*", "", "", "", "", NNewLineMode.LF));
        for (NLine nLine : lines) {
            all.add(new NElementLineImpl("", "*", " ", nLine.content(), "", "", NNewLineMode.LF));
        }
        all.add(new NElementLineImpl("", "", "", "", "", "*/", null));
        return new NElementCommentImpl(NAffixType.BLOC_COMMENT, NElementLineImpl.concatString(all), text, all.toArray(new NElementLine[0]));
    }

    public static NElementCommentImpl ofLine(String text) {
        if (text == null || text.isEmpty()) {
            return EMPTY_LINE_COMMENT;
        }
        List<NLine> lines = NLine.parseList(text);
        if (lines.isEmpty()) {
            return EMPTY_BLOC_COMMENT;
        }
        List<NElementLineImpl> all = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            NLine nLine = lines.get(i);
            all.add(new NElementLineImpl("", "//", " ", nLine.content(), "", "", (i < lines.size() - 1) ? NNewLineMode.LF : null));
        }
        return new NElementCommentImpl(NAffixType.LINE_COMMENT, NElementLineImpl.concatString(all), text, all.toArray(new NElementLine[0]));
    }

    public static NElementCommentImpl ofBloc(NElementLine... lines) {
        List<NElementLine> all = new ArrayList<>();
        if (lines != null) {
            lines = Arrays.stream(lines).filter(Objects::nonNull).toArray(NElementLine[]::new);
            for (int i = 0; i < lines.length; i++) {
                NElementLine li = lines[i];
                String content = li.content();
                if (content.contains("*/")) {
                    throw new NIllegalArgumentException(NMsg.ofC(
                            "Comment content cannot contain the end-of-block marker '*/'. " +
                                    "Found in: %s", content));
                }
                String em = li.endMarker();
                if (em.indexOf("*/") < em.length() - 2) {
                    throw new NIllegalArgumentException(NMsg.ofC(
                            "Comment endMarker cannot contain the end-of-block marker '*/'. " +
                                    "Found in: %s", em));
                }
                if (i == 0 && !li.startMarker().startsWith("/*")) {
                    all.add(new NElementLineImpl("", "/*", "", "", "", "", NNewLineMode.LF));
                }
                if (i == lines.length - 1) {
                    if (!li.startMarker().startsWith("*/")) {
                        all.add(li);
                        all.add(new NElementLineImpl("", "", "", "", "", "*/", null));
                    } else {
                        all.add(li);
                    }
                } else {
                    if (li.newline() == null) {
                        li = new NElementLineImpl(li.prefix(), li.startMarker(), li.startPadding(), li.content(), li.endPadding(), li.endMarker(), NNewLineMode.LF);
                    }
                    all.add(li);
                }
            }
        }
        if (all.isEmpty()) {
            return EMPTY_BLOC_COMMENT;
        }
        return new NElementCommentImpl(NAffixType.BLOC_COMMENT, NElementLineImpl.concatString(all), NElementLineImpl.concatContent(all), all.toArray(new NElementLine[0]));
    }

    public static NElementCommentImpl ofLine(NElementLine... lines) {
        List<NElementLine> all = new ArrayList<>();
        if (lines != null) {
            lines = Arrays.stream(lines).filter(Objects::nonNull).toArray(NElementLine[]::new);
            for (int i = 0; i < lines.length; i++) {
                NElementLine li = lines[i];
                NNewLineMode nl = li.newline();
                if (nl == null && i < lines.length - 1) {
                    nl = NNewLineMode.LF;
                }
                if (!li.startMarker().startsWith("//")) {
                    if (li.startMarker().startsWith("/")) {
                        li = new NElementLineImpl(li.prefix(), "/" + li.startMarker(), li.startPadding(), li.content(), li.endPadding(), li.endMarker(), nl);
                    } else {
                        li = new NElementLineImpl(li.prefix(), "//" + li.startMarker(), li.startPadding(), li.content(), li.endPadding(), li.endMarker(), nl);
                    }
                } else {
                    if (nl != null && li.newline() == null) {
                        li = new NElementLineImpl(li.prefix(), li.startMarker(), li.startPadding(), li.content(), li.endPadding(), li.endMarker(), nl);
                    }
                }
                all.add(li);
            }
        }
        if (all.isEmpty()) {
            return EMPTY_LINE_COMMENT;
        }
        return new NElementCommentImpl(NAffixType.LINE_COMMENT, NElementLineImpl.concatString(all), NElementLineImpl.concatContent(all), all.toArray(new NElementLine[0]));
    }

    public NElementCommentImpl(NAffixType type, String raw, String content, NElementLine... texts) {
        this.type = type;
        this.lines = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(texts)));
        this.content = content;
        this.raw = raw;
    }

    public static NElementComment of(NAffixType type, String text) {
        return type == NAffixType.BLOC_COMMENT ? ofBloc(text) : ofLine(text);
    }

    @Override
    public List<NElementLine> lines() {
        return lines;
    }

    @Override
    public NNewLineMode newlineSuffix() {
        return lines.get(lines.size() - 1).newline();
    }

    @Override
    public String raw() {
        return raw;
    }

    @Override
    public NElementComment withNewlineSuffix(NNewLineMode nNewLineMode) {
        if (type == NAffixType.BLOC_COMMENT) {
            return this;
        }
        if (newlineSuffix() == nNewLineMode) {
            return this;
        }
        List<NElementLine> lines2 = new ArrayList<>(lines);
        NElementLine l = lines2.get(lines2.size() - 1);
        l = l.withNewline(nNewLineMode);
        lines2.set(lines2.size() - 1, l);
        return new NElementCommentImpl(type, NElementLineImpl.concatString(lines2), NElementLineImpl.concatContent(lines2), lines2.toArray(new NElementLine[0]));
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(content);
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
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NElementCommentImpl that = (NElementCommentImpl) o;
        return type == that.type && Objects.equals(raw, that.raw) && Objects.equals(content, that.content) && Objects.equals(lines, that.lines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, raw, content, lines);
    }

//    @Override
//    public int compareTo(NElementComment o) {
//        int i = text().compareTo(o.text());
//        if (i != 0) {
//            return i;
//        }
//        return type.compareTo(o.type());
//    }
}

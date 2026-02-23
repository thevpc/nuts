package net.thevpc.nuts.runtime.standalone.format.tson.parser;

import net.thevpc.nuts.elem.NElementLine;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.NIllegalArgumentException;

import java.util.List;
import java.util.Objects;

public class NElementLineImpl implements NElementLine {
    private String prefix;
    private String startMarker;
    private String endMarker;
    private String startPadding;
    private String endPadding;
    private String content;
    private NNewLineMode newline;

    public NElementLineImpl(NElementLine spec, String startMarker, String endMarker) {
        this(spec.prefix(), startMarker, spec.startPadding(), spec.content(), spec.endPadding(), endMarker, spec.newline());
    }


    public NElementLineImpl(String prefix, String startMarker, String startPadding, String content, String endPadding, String endMarker, NNewLineMode newline) {
        this.prefix = ensureWhites(prefix);
        this.startPadding = ensureWhites(startPadding);
        this.startMarker = startMarker == null ? "" : startMarker;
        this.content = content == null ? "" : content;
        this.endPadding = ensureWhites(endPadding);
        this.endMarker = endMarker == null ? "" : endMarker;
        this.newline = newline == null ? null : newline.normalize();
    }

    @Override
    public String prefix() {
        return prefix;
    }

    @Override
    public String startMarker() {
        return startMarker;
    }

    @Override
    public String endMarker() {
        return endMarker;
    }

    @Override
    public String startPadding() {
        return startPadding;
    }

    @Override
    public String endPadding() {
        return endPadding;
    }

    @Override
    public String content() {
        return content;
    }

    @Override
    public NNewLineMode newline() {
        return newline;
    }

    @Override
    public NElementLine withNewline(NNewLineMode nl) {
        if (newline == nl) {
            return this;
        }
        return new NElementLineImpl(prefix, startMarker, startPadding, content, endPadding, endMarker, nl);
    }

    private String ensureWhites(String any) {
        if (any == null) {
            return "";
        }
        for (char c : any.toCharArray()) {
            if (c != ' ' && c != '\t') {
                throw new NIllegalArgumentException(NMsg.ofC("expected only spaces or tabs in '%s'", any));
            }
        }
        return any;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(startMarker);
        sb.append(startPadding);
        sb.append(content);
        sb.append(endPadding);
        sb.append(endMarker);
        if (newline != null) {
            sb.append(newline.value());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NElementLineImpl that = (NElementLineImpl) o;
        return Objects.equals(prefix, that.prefix) && Objects.equals(startMarker, that.startMarker) && Objects.equals(endMarker, that.endMarker) && Objects.equals(startPadding, that.startPadding) && Objects.equals(endPadding, that.endPadding) && Objects.equals(content, that.content) && newline == that.newline;
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, startMarker, endMarker, startPadding, endPadding, content, newline);
    }

    public static String concatString(List<? extends NElementLine> all) {
        StringBuilder sb = new StringBuilder();
        for (NElementLine nElementLine : all) {
            sb.append(nElementLine.toString());
        }
        return sb.toString();
    }

    public static String concatString(NElementLine... all) {
        StringBuilder sb = new StringBuilder();
        for (NElementLine nElementLine : all) {
            sb.append(nElementLine.toString());
        }
        return sb.toString();
    }

    public static String concatContent(List<? extends NElementLine> all) {
        StringBuilder sb = new StringBuilder();
        for (NElementLine nElementLine : all) {
            sb.append(nElementLine.content());
            if (nElementLine.newline() != null) {
                sb.append(nElementLine.newline().value());
            }
        }
        return sb.toString();
    }

    public static String concatContent(NElementLine... all) {
        StringBuilder sb = new StringBuilder();
        for (NElementLine nElementLine : all) {
            sb.append(nElementLine.content());
            if (nElementLine.newline() != null) {
                sb.append(nElementLine.newline().value());
            }
        }
        return sb.toString();
    }
}

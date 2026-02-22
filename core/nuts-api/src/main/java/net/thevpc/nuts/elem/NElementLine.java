package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NNewLineMode;

public interface NElementLine {
    static NElementLine ofElementLine(String prefix, String startMarker, String startPadding, String content, String endPadding, String endMarker, NNewLineMode newline) {
        return NElementFactory.of().ofElementLine(prefix, startMarker, startPadding, content, endPadding, endMarker, newline);
    }

    static NElementLine ofElementLine(String prefix, String startPadding, String content, String endPadding, NNewLineMode newline) {
        return NElementFactory.of().ofElementLine(prefix, null, startPadding, content, endPadding, null, newline);
    }

    String prefix();

    String startMarker();

    String endMarker();

    String startPadding();

    String endPadding();

    String content();

    NNewLineMode newline();

    NElementLine withNewline(NNewLineMode nl);
}

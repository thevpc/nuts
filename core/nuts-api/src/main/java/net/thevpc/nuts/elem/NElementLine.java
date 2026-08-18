package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.text.NNewLineMode;

public interface NElementLine {
    static NElementLine ofElementLine(String prefix, String startMarker, String startPadding, String content, String endPadding, String endMarker, NNewLineMode newline) {
        return NElementRPI.of().createElementLine(prefix, startMarker, startPadding, content, endPadding, endMarker, newline);
    }

    static NElementLine ofElementLine(String prefix, String startPadding, String content, String endPadding, NNewLineMode newline) {
        return NElementRPI.of().createElementLine(prefix, null, startPadding, content, endPadding, null, newline);
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

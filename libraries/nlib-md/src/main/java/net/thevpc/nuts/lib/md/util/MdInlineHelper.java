package net.thevpc.nuts.lib.md.util;

import net.thevpc.nuts.lib.md.MdElement;

public class MdInlineHelper {
    public static InlineMode detectInline(MdElement[] content) {
        boolean someNewline = false;
        boolean lastNewline = false;
        for (int i = 0, contentLength = content.length; i < contentLength; i++) {
            MdElement mdElement = content[i];
            if (i < contentLength - 1) {
                lastNewline = !mdElement.isInline();
                if (lastNewline) {
                    someNewline = true;
                }
            } else {
                lastNewline = !mdElement.isInline();
            }
        }
        if (someNewline && lastNewline) {
            return InlineMode.NEWLINES;
        } else if (lastNewline) {
            return InlineMode.END_NEWLINE;
        } else if (someNewline) {
            return InlineMode.MIDDLE_NEWLINE;
        } else {
            return InlineMode.NO_NEWLINES;
        }
    }

    public enum InlineMode {
        NO_NEWLINES,
        END_NEWLINE,
        MIDDLE_NEWLINE,
        NEWLINES
    }
}

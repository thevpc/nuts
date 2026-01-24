package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NNewLineMode;

public interface NElementFormatOptions {
    int getComplexityThreshold() ;
    int getIndent() ;
    int getColumnLimit();

    NNewLineMode getNewLineMode();
}

package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NNewLineMode;

public interface NElementFormatOptions {
    int complexityThreshold() ;
    int indent() ;
    int columnLimit();

    NNewLineMode newLineMode();
}

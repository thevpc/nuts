package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NMsg;

public interface NErrorElement extends NElement {
    NMsg getMessage();
    NErrorElementBuilder builder();
}

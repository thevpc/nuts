package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NMsg;

public interface NErrorElementBuilder extends NElementBuilder {
    NMsg getMessage();
    NErrorElementBuilder setMessage(NMsg msg);
    NErrorElement build();
}

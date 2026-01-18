package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.NDefaultErrorElement;
import net.thevpc.nuts.text.NMsg;

public class DefaultNErrorElementBuilder extends AbstractNElementBuilder implements NErrorElementBuilder {
    private NMsg message;

    @Override
    public NErrorElementBuilder setMessage(NMsg msg) {
        this.message = msg;
        return this;
    }

    public NMsg getMessage() {
        return message;
    }

    @Override
    public NErrorElement build() {
        return new NDefaultErrorElement(
                message
                , annotations().toArray(new NElementAnnotation[0])
                , comments()
        );
    }

    @Override
    public NElementType type() {
        return NElementType.ERROR;
    }
}

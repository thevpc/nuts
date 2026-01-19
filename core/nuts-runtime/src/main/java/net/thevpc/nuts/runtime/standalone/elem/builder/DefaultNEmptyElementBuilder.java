package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.NDefaultEmptyElement;
import net.thevpc.nuts.text.NMsg;

public class DefaultNEmptyElementBuilder extends AbstractNElementBuilder implements NEmptyElementBuilder {
    @Override
    public NEmptyElement build() {
        return new NDefaultEmptyElement(
                annotations()
                , comments()
                ,diagnostics()
        );
    }

    @Override
    public NElementType type() {
        return NElementType.EMPTY;
    }
}

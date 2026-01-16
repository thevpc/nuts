package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NListElement;
import net.thevpc.nuts.elem.NListItemElement;
import net.thevpc.nuts.elem.NListItemElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNListItemElement;

public class DefaultNListItemElementBuilder implements NListItemElementBuilder {
    private int depth;
    private NElement value;
    private NListElement subList;

    public DefaultNListItemElementBuilder(int depth) {
        this.depth = depth;
    }

    public NListItemElementBuilder value(NElement value) {
        this.value = value;
        return this;
    }

    public NListItemElementBuilder subList(NListElement subList) {
        this.subList = subList;
        return this;
    }

    public NListItemElement build() {
        return new DefaultNListItemElement(depth, value, subList);
    }
}

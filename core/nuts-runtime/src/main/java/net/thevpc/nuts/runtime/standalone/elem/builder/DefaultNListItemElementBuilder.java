package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNListItemElement;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;

public class DefaultNListItemElementBuilder implements NListItemElementBuilder {
    private int depth;
    private String variant;
    private NElement value;
    private NListElement subList;

    public DefaultNListItemElementBuilder(String variant, int depth) {
        this.depth = depth;
        this.variant = variant;
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
        String image;
        if (variant.startsWith("[")) {
            image = "[" + NStringUtils.repeat(variant.charAt(1), depth) + "]";
        } else {
            image = NStringUtils.repeat(variant.charAt(1), depth);
        }
        return new DefaultNListItemElement(
                NElementType.UNORDERED_LIST,
                image,
                variant,
                depth, value, subList,new ArrayList<>()
        );
    }
}

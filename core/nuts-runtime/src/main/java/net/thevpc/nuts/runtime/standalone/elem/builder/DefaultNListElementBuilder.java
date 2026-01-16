package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNListElement;

import java.util.ArrayList;
import java.util.List;

public class DefaultNListElementBuilder extends AbstractNElementBuilder implements NListElementBuilder {
    private NElementType type;
    private int depth;
    private List<NListItemElement> items = new ArrayList<>();

    public DefaultNListElementBuilder(NElementType type, int depth) {
        this.type = type;
        this.depth = depth;
    }

    public NListElementBuilder addItem(NListItemElement item) {
        this.items.add(item);
        return this;
    }

    public NListElement build() {
        List<NListItemElement> builtItems = new ArrayList<>();
        for (NListItemElement item : items) {
            builtItems.add(item);
        }
        return new DefaultNListElement(
                type == null ? NElementType.UNORDERED_LIST : type,
                depth <= 0 ? 1 : depth, items, null, null
        );
    }

    // For internal stack use
    public int depth() {
        return depth;
    }

    public NElementType type() {
        return type;
    }

    @Override
    public NListItemElement getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public List<NListItemElement> items() {
        return new ArrayList<>(items);
    }
}

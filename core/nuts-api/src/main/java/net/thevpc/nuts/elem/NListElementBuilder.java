package net.thevpc.nuts.elem;

import java.util.List;

public interface NListElementBuilder extends NElementBuilder {
    int depth();

    NListElementBuilder addItem(NListItemElement item);

    NListItemElement getItem(int index);
    List<NListItemElement> items();

    int size();
}

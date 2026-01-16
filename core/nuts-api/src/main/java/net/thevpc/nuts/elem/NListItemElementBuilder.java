package net.thevpc.nuts.elem;

public interface NListItemElementBuilder{

    NListItemElementBuilder value(NElement value);
    NListItemElementBuilder subList(NListElement subList);
    NListItemElement build() ;
}

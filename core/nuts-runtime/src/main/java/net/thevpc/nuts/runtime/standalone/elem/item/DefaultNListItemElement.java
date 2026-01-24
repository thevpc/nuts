package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Objects;

public class DefaultNListItemElement implements NListItemElement {
    private String marker;
    private String markerVariant;
    private int depth;
    private NElement value;
    private NListElement subList;
    private NElementType listType;
    private List<NBoundAffix> affixes;

    public DefaultNListItemElement(NElementType listType, String marker, String markerVariant, int depth, NElement value, NListElement subList, List<NBoundAffix> affixes) {
        this.marker = marker;
        this.markerVariant = markerVariant;
        this.depth = depth;
        this.value = value;
        this.subList = subList;
        this.listType = listType;
        this.affixes = CoreNUtils.copyNonNullUnmodifiableList(affixes);
    }

    @Override
    public int depth() {
        return depth;
    }

    public List<NBoundAffix> affixes() {
        return affixes;
    }

    public NElementType listType() {
        return listType;
    }

    @Override
    public String marker() {
        return marker;
    }

    @Override
    public String markerVariant() {
        return markerVariant;
    }

    @Override
    public NOptional<NElement> value() {
        return NOptional.ofNamed(value, "value");
    }

    @Override
    public NOptional<NListElement> subList() {
        return NOptional.ofNamed(subList, "subList");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNListItemElement that = (DefaultNListItemElement) o;
        return depth == that.depth && Objects.equals(value, that.value) && Objects.equals(subList, that.subList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(depth, value, subList);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (value != null && subList != null) {
            sb.append(value);
            sb.append("\n");
            sb.append(subList);
        } else {
            if (value != null) {
                sb.append(value);
            }
            if (subList != null) {
                sb.append(subList);
            }
        }
        return sb.toString();
    }
}

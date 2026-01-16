package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NListElement;
import net.thevpc.nuts.elem.NListItemElement;
import net.thevpc.nuts.util.NOptional;

import java.util.Objects;

public class DefaultNListItemElement implements NListItemElement {
    private int depth;
    private NElement value;
    private NListElement subList;

    public DefaultNListItemElement(int depth, NElement value, NListElement subList) {
        this.depth = depth;
        this.value = value;
        this.subList = subList;
    }

    @Override
    public int depth() {
        return depth;
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
        }else {
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

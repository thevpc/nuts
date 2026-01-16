package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DefaultNListElement extends AbstractNElement implements NListElement {
    private int depth;
    private List<NListItemElement> children;

    public DefaultNListElement(NElementType type, int depth, List<NListItemElement> children, NElementAnnotation[] annotations, NElementComments comments) {
        super(type, annotations, comments);
        this.depth = depth;
        this.children = Collections.unmodifiableList(new ArrayList<>(children));
    }

    @Override
    public int depth() {
        return depth;
    }

    @Override
    public List<NListItemElement> children() {
        return children;
    }

    @Override
    public String toString(boolean compact) {
        switch (type()) {
            case ORDERED_LIST: {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < children.size(); i++) {
                    NListItemElement child = children.get(i);
                    if (i > 0) {
                        sb.append("\n");
                    }
                    for (int j = 0; j < child.depth(); j++) {
                        sb.append("#");
                    }
                    sb.append(" ");
                    sb.append(child.toString());
                }
                return sb.toString();
            }
            case UNORDERED_LIST: {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < children.size(); i++) {
                    NListItemElement child = children.get(i);
                    if (i > 0) {
                        sb.append("\n");
                    }
                    for (int j = 0; j < child.depth(); j++) {
                        sb.append(".");
                    }
                    sb.append(" ");
                    sb.append(child.toString());
                }
                return sb.toString();
            }
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultNListElement that = (DefaultNListElement) o;
        return depth == that.depth && Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), depth, children);
    }

    @Override
    public int size() {
        return children.size();
    }

    @Override
    public String toString() {
        return toString(false);
    }
}

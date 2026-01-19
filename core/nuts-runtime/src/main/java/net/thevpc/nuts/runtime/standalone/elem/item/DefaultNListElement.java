package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.text.NTreeVisitResult;

import java.util.*;

public class DefaultNListElement extends AbstractNElement implements NListElement {
    private int depth;
    private List<NListItemElement> children;

    public DefaultNListElement(NElementType type, int depth, List<NListItemElement> children) {
        this(type,depth,children,null,null,null);
    }

    public DefaultNListElement(NElementType type, int depth, List<NListItemElement> children, List<NElementAnnotation> annotations, NElementComments comments, List<NElementDiagnostic> diagnostics) {
        super(type, annotations, comments, diagnostics);
        this.depth = depth;
        this.children = CoreNUtils.copyAndUnmodifiableList(children);
    }

    protected NTreeVisitResult traverseChildren(NElementVisitor visitor) {
        for (NListItemElement element : children) {
            NElement n = element.value().orNull();
            if (n != null) {
                NTreeVisitResult result = n.traverse(visitor);
                if (result == NTreeVisitResult.TERMINATE) {
                    return result;
                }
                if (result == NTreeVisitResult.SKIP_SIBLINGS) {
                    break;
                }
            }
            NListElement subList = element.subList().orNull();
            if (subList != null) {
                NTreeVisitResult result = subList.traverse(visitor);
                if (result != NTreeVisitResult.CONTINUE) {
                    return result;
                }
            }
        }
        return NTreeVisitResult.CONTINUE;
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

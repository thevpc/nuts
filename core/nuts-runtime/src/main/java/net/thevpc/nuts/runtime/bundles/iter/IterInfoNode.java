package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsElements;
import net.thevpc.nuts.NutsPredicates;
import net.thevpc.nuts.NutsSession;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class IterInfoNode {
    private final String name;
    private final String type;
    private final String description;
    private final float weight;
    private final Object value;
    private List<IterInfoNode> children;

    public IterInfoNode(IterInfoNode n, IterInfoNode... children) {
        if (n != null) {
            this.name = n.name;
            this.type = n.type;
            this.description = n.description;
            this.weight = n.weight;
            this.value = n.value;
            if (n.children != null) {
                for (IterInfoNode e : n.children) {
                    addChild(e);
                }
            }
        } else {
            this.name = null;
            this.type = null;
            this.description = null;
            this.weight = 0;
            this.value = null;
        }
        if (children != null) {
            for (IterInfoNode child : children) {
                addChild(child);
            }
        }
    }

    public IterInfoNode(String name, String type, String description, float weight, Object value, IterInfoNode... children) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.weight = weight;
        this.value = value;
        if (children != null) {
            for (IterInfoNode child : children) {
                addChild(child);
            }
        }
    }

    public IterInfoNode(String name, String type, String description, float weight, Object value, List<IterInfoNode> children) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.weight = weight;
        this.value = value;
        if (children != null) {
            for (IterInfoNode e : children) {
                addChild(e);
            }
        }
    }

    public static IterInfoNode ofLiteral(String name, String description, Object value, IterInfoNode... children) {
        return new IterInfoNode(
                name, null, description, 0, value, children
        );
    }

    public static IterInfoNode ofLiteralType(String type, String name, String description, Object value, IterInfoNode... children) {
        return new IterInfoNode(
                name, type, description, 0, value, children
        );
    }

    public static IterInfoNode resolveOrStringNonNull(String name, Object o, NutsSession session) {
        if(o==null){
            return null;
        }
        return resolveOrString(name,o, session);
    }

    public static IterInfoNode resolveOrString(String type, Object o, NutsSession session) {
        Predicate<Type> destructTypeFilter = NutsElements.of(session).setIndestructibleFormat().getIndestructibleObjects();
        IterInfoNode a = resolveOrNull(null, o, session);
        if (a == null) {
            if(o==null || destructTypeFilter.test(o.getClass())){
                return ofLiteralType(type,null, null, o);
            }
            return ofLiteralType(type,null, null, String.valueOf(o));
        }
        return a;
    }

    public static IterInfoNode resolveOrNull(String name, Object o, NutsSession session) {
        IterInfoNode q = resolveOrNull(o, session);
        if (q != null && name != null) {
            return q.withName(name);
        }
        return q;
    }

    public static IterInfoNode resolveOrNull(Object o, NutsSession session) {
        if (o instanceof IterInfoNode) {
            return (IterInfoNode) o;
        }
        if (o instanceof IterInfoNodeAware) {
            return ((IterInfoNodeAware) o).info(session);
        }
        if (o instanceof NutsPredicates.BaseOpPredicate) {
            return IterInfoNode.ofLiteralType("Predicate", "Predicate", null, o.toString());
        }
        return null;
    }

    private void addChild(IterInfoNode child) {
        if (child != null) {
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(child);
        }
    }


    public String getName() {
        return name;
    }

    public IterInfoNode withType(String type) {
        return new IterInfoNode(
                name, type, description, weight, value, children
        );
    }

    public IterInfoNode withNonNullName(String name) {
        if (name != null) {
            return withName(name);
        }
        return this;
    }

    public IterInfoNode withName(String name) {
        return new IterInfoNode(
                name, type, description, weight, value, children
        );
    }

    public IterInfoNode withDescription(String description) {
        return new IterInfoNode(
                name, type, description, weight, value, children
        );
    }

    public IterInfoNode withWeight(float weight) {
        return new IterInfoNode(
                name, type, description, weight, value, children
        );
    }

    public IterInfoNode withValue(Object value) {
        return new IterInfoNode(
                name, type, description, weight, value, children
        );
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public float getWeight() {
        return weight;
    }

    public Object getValue() {
        return value;
    }

    public IterInfoNode[] getChildren() {
        return children == null ? new IterInfoNode[0] : children.toArray(new IterInfoNode[0]);
    }
}

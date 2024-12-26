package net.thevpc.nuts.runtime.standalone.util.reflect.mapper;

import java.util.HashMap;
import java.util.Map;

public class TypeMapperTraversedTreeImpl implements TypeMapperTraversedTree {
    private final Map<TraversedTreeKey, Object> traversedTree = new HashMap<>();

    public Object get(Object a) {
        return traversedTree.get(new TraversedTreeKey(a));
    }

    public Object put(Object a, Object b) {
        return traversedTree.put(new TraversedTreeKey(a), b);
    }
}

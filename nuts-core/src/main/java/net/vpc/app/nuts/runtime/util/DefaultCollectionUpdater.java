package net.vpc.app.nuts.runtime.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultCollectionUpdater<T> {
    private List<T> added=new ArrayList<>();
    private Set<T> removed=new HashSet<>();
    private Set<Integer> removedIndices=new HashSet<>();

    public void undoAdd(T t){
        added.remove(t);
    }

    public void add(T t){
        added.add(t);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.NutsIterator;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElements;
import net.thevpc.nuts.NutsDescribables;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 *
 * @author thevpc
 */
public class CoalesceIterator<T> extends NutsIteratorBase<T> {

    private Queue<NutsIterator<? extends T>> children = new LinkedList<NutsIterator<? extends T>>();
    private int size = 0;

    @Override
    public NutsElement describe(NutsElements elems) {
        return elems.ofObject()
                .set("type", "Coalesce")
                .set("items",
                        elems.ofArray()
                                .addAll(
                                        new ArrayList<>(children)
                                                .stream().map(
                                                        x-> NutsDescribables.resolveOrDestruct(x,elems)
                                                ).collect(Collectors.toList())
                                )
                                .build()
                )
                .build();
    }


    public void addNonNull(NutsIterator<T> child) {
        if (child != null) {
            add(child);
        }
    }

    public void addNonEmpty(NutsIterator<T> child) {
        child = IteratorUtils.nullifyIfEmpty(child);
        if (child != null) {
            add(child);
        }
    }

    public void add(NutsIterator<? extends T> child) {
        if (child == null) {
            throw new NullPointerException();
        }
        children.add(child);
        size++;
    }

    public int size() {
        return children.size();
    }
    
    public NutsIterator<T>[] getChildren() {
        return children.toArray(new NutsIterator[0]);
    }

    @Override
    public boolean hasNext() {
        while (!children.isEmpty()) {
            if (children.peek().hasNext()) {
                if (size > 1) {
                    //should remove all successors;
                    NutsIterator<? extends T> h = children.poll();
                    children.clear();
                    children.offer(h);
                    size = 1;
                }
                return true;
            }
            children.poll();
            size--;
        }
        return false;
    }

    public T next() {
        return children.peek().next();
    }

    public void remove() {
        children.peek().remove();
    }

    @Override
    public String toString() {
        return "CoalesceIterator(" +
                children +
                ')';
    }
}

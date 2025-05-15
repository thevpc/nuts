/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class NCoalesceIterator<T> extends NIteratorBase<T> {

    private Queue<NIterator<? extends T>> children = new LinkedList<NIterator<? extends T>>();
    private int size = 0;

    @Override
    public NElement describe() {
        return NElements.of().ofObjectBuilder()
                .name("Coalesce")
                .addAll(
                        children
                                .stream().map(
                                        x -> NEDesc.describeResolveOrDestruct(x)
                                ).toArray(NElement[]::new)
                )
                .build();
    }


    public void addNonNull(NIterator<T> child) {
        if (child != null) {
            add(child);
        }
    }

    public void addNonEmpty(NIterator<T> child) {
        child = NIteratorUtils.nullifyIfEmpty(child);
        if (child != null) {
            add(child);
        }
    }

    public void add(NIterator<? extends T> child) {
        if (child == null) {
            throw new NullPointerException();
        }
        if (child instanceof NCoalesceIterator) {
            children.addAll(((NCoalesceIterator<? extends T>) child).children);
        } else {
            children.add(child);
        }
        size=children.size();
    }

    public int size() {
        return children.size();
    }

    public NIterator<T>[] getChildren() {
        return children.toArray(new NIterator[0]);
    }

    @Override
    public boolean hasNext() {
        while (!children.isEmpty()) {
            if (children.peek().hasNext()) {
                if (size > 1) {
                    //should remove all successors;
                    NIterator<? extends T> h = children.poll();
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
        return "Coalesce(" +
                children +
                ')';
    }
}

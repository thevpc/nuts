/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.util.NIterator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 *
 * @author thevpc
 */
public class CoalesceIterator<T> extends NIteratorBase<T> {

    private Queue<NIterator<? extends T>> children = new LinkedList<NIterator<? extends T>>();
    private int size = 0;

    @Override
    public NElement describe(NSession session) {
        return NElements.of(session).ofObject()
                .set("type", "Coalesce")
                .set("items",
                        NElements.of(session).ofArray()
                                .addAll(
                                        new ArrayList<>(children)
                                                .stream().map(
                                                        x-> NEDesc.describeResolveOrDestruct(x, session)
                                                ).collect(Collectors.toList())
                                )
                                .build()
                )
                .build();
    }


    public void addNonNull(NIterator<T> child) {
        if (child != null) {
            add(child);
        }
    }

    public void addNonEmpty(NIterator<T> child) {
        child = IteratorUtils.nullifyIfEmpty(child);
        if (child != null) {
            add(child);
        }
    }

    public void add(NIterator<? extends T> child) {
        if (child == null) {
            throw new NullPointerException();
        }
        children.add(child);
        size++;
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
        return "CoalesceIterator(" +
                children +
                ')';
    }
}

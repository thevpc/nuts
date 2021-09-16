/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.bundles.iter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;

/**
 *
 * @author thevpc
 */
public class CoalesceIterator<T> implements Iterator<T> {

    private Queue<Iterator<? extends T>> children = new LinkedList<Iterator<? extends T>>();
    private int size = 0;

    public void addNonNull(Iterator<T> child) {
        if (child != null) {
            add(child);
        }
    }

    public void addNonEmpty(Iterator<T> child) {
        child = IteratorUtils.nullifyIfEmpty(child);
        if (child != null) {
            add(child);
        }
    }

    public void add(Iterator<? extends T> child) {
        if (child == null) {
            throw new NullPointerException();
        }
        children.add(child);
        size++;
    }

    public int size() {
        return children.size();
    }
    
    public Iterator<T>[] getChildren() {
        return children.toArray(new Iterator[0]);
    }

    @Override
    public boolean hasNext() {
        while (!children.isEmpty()) {
            if (children.peek().hasNext()) {
                if (size > 1) {
                    //should remove all successors;
                    Iterator<? extends T> h = children.poll();
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

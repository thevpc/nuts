/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.common;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author vpc
 */
public class CoalesceIterator<T> implements Iterator<T> {

    private Queue<Iterator<T>> children = new LinkedList<Iterator<T>>();
    private int size = 0;
//    public static void main(String[] args) {
//        CoalesceIterator<String> q=new CoalesceIterator<String>();
//        q.add(Collections.<String>emptyIterator());
//        q.add(Arrays.asList("c","d").iterator());
//        q.add(Arrays.asList("e","f").iterator());
//        while(q.hasNext()){
//            System.out.println(q.next());
//        }
//    }

    public void addNonNull(Iterator<T> child) {
        if (child != null) {
            add(child);
        }
    }

    public void addNonEmpty(Iterator<T> child) {
        child = CoreCommonUtils.nullifyIfEmpty(child);
        if (child != null) {
            add(child);
        }
    }

    public void add(Iterator<T> child) {
        if (child == null) {
            throw new NullPointerException();
        }
        children.add(child);
        size++;
    }

    public boolean hasNext() {
        while (!children.isEmpty()) {
            if (children.peek().hasNext()) {
                if (size > 1) {
                    //should remove all successors;
                    Iterator<T> h = children.poll();
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

}

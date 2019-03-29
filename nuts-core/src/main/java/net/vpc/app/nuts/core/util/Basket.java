/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.vpc.common.util.CollectionUtils;

/**
 *
 * @author vpc
 */
public class Basket<T> {
    
    private final Object o;
    private final char type;

    public Basket(Iterator<T> o) {
        this.o = o;
        this.type = 'i';
    }

    public Basket(Collection<T> o) {
        this.o = o;
        this.type = (o instanceof List) ? 'l' : 'c';
    }

    public Basket(List<T> o) {
        this.o = o;
        this.type = 'l';
    }

    public List<T> list() {
        switch (type) {
            case 'i':
                return CollectionUtils.toList((Iterator<T>) o);
            case 'l':
                return (List<T>) o;
            case 'c':
                return new ArrayList<>((Collection<T>) o);
        }
        throw new IllegalArgumentException("Illegal type");
    }

    public Iterator<T> iterator() {
        switch (type) {
            case 'i':
                return (Iterator<T>) o;
            case 'l':
                return ((Collection<T>) o).iterator();
            case 'c':
                return ((Collection<T>) o).iterator();
        }
        throw new IllegalArgumentException("Illegal type");
    }

    public Stream<T> stream() {
        switch (type) {
            case 'i':
                return StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator<T>) o, Spliterator.ORDERED), false);
            case 'l':
                return ((Collection<T>) o).stream();
            case 'c':
                return ((Collection<T>) o).stream();
        }
        throw new IllegalArgumentException("Illegal type");
    }
    
}

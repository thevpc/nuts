/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.bundledlibs.util;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.vpc.app.nuts.core.util.CoreCommonUtils;

/**
 *
 * @author vpc
 */
public class IteratorBuilder<T> {

    private final Iterator<T> it;

    public static <T> IteratorBuilder<T> of(Iterator<T> t) {
        return new IteratorBuilder<>(t);
    }

    public static <T> IteratorBuilder<T> ofLazy(Iterable<T> t) {
        return new IteratorBuilder<>(
                new LazyIterator(t)
        );
    }

    public static IteratorBuilder<File> ofFileDfs(File file) {
        return of(new FileDepthFirstIterator(file));
    }

    public static <T> IteratorBuilder<T> ofArray(T... t) {
        return of(t == null ? Collections.<T>emptyIterator() : Arrays.asList(t).iterator());
    }

    public static IteratorBuilder<File> ofFileList(File file) {
        return ofArray(file.listFiles());
    }

    public static IteratorBuilder<File> ofFileList(File file, boolean intcludeSelf) {
        if (intcludeSelf) {
            return ofArray(file).concat(ofArray(file.listFiles()));
        }
        return ofArray(file.listFiles());
    }

    public IteratorBuilder<T> filter(Predicate<T> t) {
        if (t == null) {
            return this;
        }
        return new IteratorBuilder<>(new FilteredIterator<>(it, t));
    }

    public IteratorBuilder<T> concat(IteratorBuilder<T> t) {
        return concat(t.it);
    }

    public IteratorBuilder<T> concat(Iterator<T> t) {
        if (t == null) {
            return this;
        }
        return new IteratorBuilder<>(IteratorUtils.concat(Arrays.asList(it, t)));
    }

    public <V> IteratorBuilder<V> map(Function<T, V> t) {
        return new IteratorBuilder<>(new ConvertedIterator<>(it, t));
    }

    public <V> IteratorBuilder<V> convert(Function<T, V> t) {
        return new IteratorBuilder<>(new ConvertedIterator<>(it, t));
    }

    public <V> IteratorBuilder<V> mapMulti(Function<T, List<V>> t) {
        return new IteratorBuilder<>(new ConvertedToListIterator<>(it, t));
    }

    public <V> IteratorBuilder<V> convertMulti(Function<T, List<V>> t) {
        return new IteratorBuilder<>(new ConvertedToListIterator<>(it, t));
    }

    public <V> IteratorBuilder<T> unique(Function<T, V> t) {
        if (t == null) {
            return new IteratorBuilder<>(IteratorUtils.unique(it));
        } else {
            return new IteratorBuilder<>(IteratorUtils.unique(it, t));
        }
    }

    public IteratorBuilder<T> safe(ErrorHandlerIteratorType type) {
        return new IteratorBuilder<>(new ErrorHandlerIterator(type, it));
    }

    public IteratorBuilder<T> safeIgnore() {
        return safe(ErrorHandlerIteratorType.IGNORE);
    }

    public IteratorBuilder<T> safePospone() {
        return safe(ErrorHandlerIteratorType.POSPONE);
    }

    public IteratorBuilder<T> nonNull() {
        return filter(IteratorUtils.NON_NULL);
    }

    private IteratorBuilder(Iterator<T> it) {
        this.it = it;
    }

    public Iterator<T> iterator() {
        return it;
    }

    public List<T> list() {
        return CoreCommonUtils.toList(it);
    }

    public Iterator<T> build() {
        return it;
    }

}

package net.thevpc.nuts.util;

public interface NBufferedGenerator<T> extends NGenerator<T> {
    int buffered();

    T peekAt(int offset) ;

    T peek() ;

    boolean hasNext() ;

    boolean hasNext(int count) ;


    boolean skip(int count) ;
}

package net.thevpc.nuts.util;

/**
 * NBufferedGenerator interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NBufferedGenerator<T> extends NGenerator<T> {
    /**
     * Buffered.
     *
     * @return buffered result
     */
    int buffered();

    /**
     * Peek at.
     *
     * @param offset offset
     * @return peek at result
     */
    T peekAt(int offset) ;

    /**
     * Peek.
     *
     * @return peek result
     */
    T peek() ;

    /**
     * Checks if has next.
     *
     * @return has next result
     */
    boolean hasNext() ;

    /**
     * Checks if has next.
     *
     * @param count count
     * @return has next result
     */
    boolean hasNext(int count) ;


    /**
     * Skip.
     *
     * @param count count
     * @return skip result
     */
    boolean skip(int count) ;
}

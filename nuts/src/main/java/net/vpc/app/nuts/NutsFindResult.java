/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author vpc
 */
public interface NutsFindResult<T> extends Iterable<T> {

    /**
     * consumes the result and returns a list Calling this method twice will
     * result in unexpected behavior (may return an empty list as the result is
     * already consumed or throw an Exception)
     *
     * @return
     */
    List<T> list();

    /**
     *
     * Calling this method twice will result in unexpected behavior (may return
     * an incorrect value such as null as the result is already consumed or
     * throw an Exception)
     *
     * @return the first value of null if none found
     */
    T first();

    /**
     *
     * Calling this method twice will result in unexpected behavior (may return
     * an incorrect value such as null as the result is already consumed or
     * throw an Exception)
     *
     * @return the first value while checking that there are no more elements to
     * consume. An IllegalArgumentException is thrown if there are no elements
     * to consume. An IllegalArgumentException is also thrown if the are more
     * than one element consumed
     */
    T singleton() throws NutsTooManyElementsException, NutsMissingElementsException;

    /**
     *
     * Calling this method twice will result in unexpected behavior (may return
     * 0 as the result is already consumed or throw an Exception)
     *
     * @return
     */
    Stream<T> stream();

    /**
     * consumes the result and returns the number of elements consumed. Calling
     * this method twice will result in unexpected behavior (may return 0 as the
     * result is already consumed or throw an Exception)
     *
     * @return elements count of this result.
     */
    long count();

}

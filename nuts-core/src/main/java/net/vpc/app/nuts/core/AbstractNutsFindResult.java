/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.util.Iterator;
import net.vpc.app.nuts.NutsFindResult;
import net.vpc.app.nuts.NutsMissingElementsException;
import net.vpc.app.nuts.NutsTooManyElementsException;

/**
 *
 * @author vpc
 * @param <T>
 */
public abstract class AbstractNutsFindResult<T> implements NutsFindResult<T> {

    @Override
    public T first() {
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    @Override
    public T singleton() {
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            T t = it.next();
            if (it.hasNext()) {
                throw new NutsTooManyElementsException();
            }
            return t;
        } else {
            throw new NutsMissingElementsException();
        }
    }

    @Override
    public long count() {
        long count = 0;
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            count++;
        }
        return count;
    }
}

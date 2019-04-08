/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsFetchResult;
import net.vpc.app.nuts.NutsNotFoundException;

/**
 *
 * @author vpc
 */
public abstract class AbstractNutsFetchResult<T> implements NutsFetchResult<T> {

    @Override
    public T getOrNull() {
        try {
            return get();
        } catch (NutsNotFoundException ex) {
            return null;
        }
    }

}

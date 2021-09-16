/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.bundles.iter;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Created by vpc on 1/9/17.
 *
 * @param <F> From Type
 * @param <T> To Type
 */
public class ConvertedIterator<F, T> implements Iterator<T> {

    private final Iterator<F> base;
    private final Function<? super F, ? extends T> converter;
    private final String convertName;

    public ConvertedIterator(Iterator<F> base, Function<? super F, ? extends T> converter,String convertName) {
        this.base = base;
        this.converter = converter;
        if(convertName==null){
            convertName=this.converter.toString();
        }
        this.convertName = convertName;
    }

    @Override
    public boolean hasNext() {
        return base.hasNext();
    }

    @Override
    public T next() {
        return converter.apply(base.next());
    }

    @Override
    public void remove() {
        base.remove();
    }

    @Override
    public String toString() {
        return "ConvertedIterator("+base+","+convertName+")";
    }
}

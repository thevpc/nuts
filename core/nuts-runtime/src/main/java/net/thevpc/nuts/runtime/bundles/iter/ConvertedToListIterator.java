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
 *
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

import net.thevpc.nuts.NutsSession;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by vpc on 1/9/17.
 *
 * @param <F> From Type
 * @param <T> To Type
 */
public class ConvertedToListIterator<F, T> extends IterInfoNodeAware2Base<T> {

    private final Iterator<F> base;
    private final Function<F, List<T>> converter;
    private final LinkedList<T> current = new LinkedList<>();

    public ConvertedToListIterator(Iterator<F> base, Function<F, List<T>> converter) {
        this.base = base;
        this.converter = converter;
    }

    @Override
    public IterInfoNode info(NutsSession session) {
        return info("ConvertedToList",
                IterInfoNode.resolveOrNull("base",base, session),
                IterInfoNode.resolveOrNull("converter",converter, session)
        );
    }

    @Override
    public boolean hasNext() {
        if (!current.isEmpty()) {
            return true;
        }
        while (base.hasNext()) {
            F f = base.next();
            List<T> c = converter.apply(f);
            current.addAll(c);
            if (!current.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public T next() {
        return current.poll();
    }

    @Override
    public void remove() {
        base.remove();
    }
}

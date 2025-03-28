/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Created by vpc on 1/9/17.
 */
public class NFilteredIterator<T> extends NIteratorBase<T> {

    private final Iterator<T> base;
    private final Predicate<? super T> filter;
    private T last;

    public NFilteredIterator(Iterator<T> base, Predicate<? super T> filter) {
        if (base == null) {
            this.base = NIteratorBuilder.emptyIterator();
        } else {
            this.base = base;
        }
        //Predicate<? super T>
//        NDescribables.cast(filter);
        this.filter = filter;
    }

    @Override
    public NElement describe() {
        return NElements.of()
                .ofObjectBuilder()
                .name("Filter")
                .set("base", NEDesc.describeResolveOrDestruct(base))
                .set("accept", NEDesc.describeResolveOrToString(filter))
                .build()
                ;
    }

    @Override
    public boolean hasNext() {
        while (true) {
            if (base.hasNext()) {
                last = base.next();
                if (filter.test(last)) {
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    @Override
    public T next() {
        return last;
    }

    @Override
    public void remove() {
        base.remove();
    }

    @Override
    public String toString() {
        return filter.toString() + "(" +
                base +
                ')';
    }
}

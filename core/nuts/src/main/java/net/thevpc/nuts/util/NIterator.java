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
package net.thevpc.nuts.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Describable Iterator
 * @param <T> T
 */
public interface NIterator<T> extends Iterator<T>, NDescribable {
    static <T> NIterator<T> of(Iterator<T> o, String descr) {
        return NDescribables.ofIterator(o, session -> NElements.of(session).ofString(descr));
    }

    static <T> NIterator<T> of(Iterator<T> o, NElement descr) {
        return NDescribables.ofIterator(o, e -> descr);
    }

    static <T> NIterator<T> of(Iterator<T> o, Function<NSession, NElement> descr) {
        return NDescribables.ofIterator(o, descr);
    }

    @SuppressWarnings("unchecked")
    static <T> NIterator<T> ofEmpty(NSession session) {
        return (NIterator<T>) NStream.ofEmpty(session).iterator();
    }
    default List<T> toList() {
        List<T> list=new ArrayList<>();
        while(hasNext()){
            list.add(next());
        }
        return list;
    }
}

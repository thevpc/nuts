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
package net.thevpc.nuts.runtime.core.commands.ws;

import net.thevpc.nuts.*;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @param <T> Type
 * @author thevpc
 */
public abstract class AbstractNutsResultList<T> implements NutsResultList<T> {

    protected NutsSession session;
    private String nutsBase;

    public AbstractNutsResultList(NutsSession session, String nutsBase) {
        this.session = session;
        this.nutsBase = nutsBase;
    }

    @Override
    public List<T> list() {
        List<T> list = new ArrayList<>();
        for (T a : this) {
            list.add(a);
        }
        return list;
    }

    @Override
    public T first() {
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    @Override
    public T required() throws NutsNotFoundException {
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            return it.next();
        }
        NutsId n = session.getWorkspace().id().parser().setLenient(true).parse(nutsBase);
        if (n != null) {
            throw new NutsNotFoundException(session, n);
        }
        throw new NutsNotFoundException(session, null, NutsMessage.cstyle("artifact not found: %s%s", (nutsBase == null ? "<null>" : nutsBase)), null);
    }

    @Override
    public T singleton() {
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            T t = it.next();
            if (it.hasNext()) {
                throw new NutsTooManyElementsException(session, NutsMessage.cstyle("too many results for %s", nutsBase));
            }
            return t;
        } else {
            NutsId nid = session.getWorkspace().id().parser().setLenient(true).parse(nutsBase);
            if (nid != null) {
                throw new NutsNotFoundException(session, nid);
            }
            throw new NutsNotFoundException(session, null, NutsMessage.cstyle("result not found for %s", nutsBase));
        }
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator<T>) iterator(), Spliterator.ORDERED), false);
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

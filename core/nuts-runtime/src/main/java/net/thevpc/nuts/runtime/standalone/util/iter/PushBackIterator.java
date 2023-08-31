/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
package net.thevpc.nuts.runtime.standalone.util.iter;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.NSession;

import java.util.Iterator;

/**
 * Created by vpc on 3/19/17.
 */
public class PushBackIterator<T> extends NIteratorBase<T> {

    private Iterator<T> base;
    private Boolean lastHasNext;
    private boolean lastValConsumed;
    private T lastVal;

    public PushBackIterator(Iterator<T> base) {
        this.base = base;
    }

    @Override
    public NElement describe(NSession session) {
        return NDescribables.resolveOrDestructAsObject(base, session)
                .builder()
                .set("pushBack",true)
                .build()
                ;
    }

    public boolean isEmpty() {
        if (hasNext()) {
            pushBack();
            return false;
        }
        return true;
    }

    @Override
    public boolean hasNext() {
        if (lastHasNext != null) {
            return lastHasNext;
        }
        if (base.hasNext()) {
            lastValConsumed = false;
            lastHasNext = true;
            lastVal = base.next();
        } else {
            lastHasNext = false;
            lastValConsumed = false;
            lastHasNext = false;
        }
        return lastHasNext;
    }

    @Override
    public T next() {
        lastHasNext = null;
        lastValConsumed = true;
        return lastVal;
    }

    @Override
    public void remove() {
        if (lastHasNext == null) {
            throw new UnsupportedOperationException("unsupported");
        }
        base.remove();
    }

    public void pushBack() {
        if (lastHasNext == null || !lastValConsumed) {
            lastHasNext = true;
        } else {
            throw new UnsupportedOperationException("unsupported");
        }
    }
}

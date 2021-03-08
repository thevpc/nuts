/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author vpc
 */
public final class NutsTextNodeStyles implements Iterable<NutsTextNodeStyle> {

    public static NutsTextNodeStyles NONE = new NutsTextNodeStyles(new NutsTextNodeStyle[0]);

    private NutsTextNodeStyle[] elements;

    private NutsTextNodeStyles(NutsTextNodeStyle[] elements) {
        this.elements = Arrays.copyOf(elements, elements.length);
    }

    public static NutsTextNodeStyles of(NutsTextNodeStyle... others) {
        return NONE.append(others);
    }

    public static NutsTextNodeStyles of(NutsTextNodeStyle other) {
        if (other == null) {
            return NONE;
        }
        return new NutsTextNodeStyles(new NutsTextNodeStyle[]{other});
    }

    public NutsTextNodeStyles append(NutsTextNodeStyles other) {
        if (other == null || other.isNone()) {
            return this;
        }
        if (this.isNone()) {
            return other;
        }
        return append(other.elements);
    }

    public NutsTextNodeStyles append(NutsTextNodeStyle... others) {
        if (others.length == 0) {
            return this;
        }
        List<NutsTextNodeStyle> all = new ArrayList<NutsTextNodeStyle>(size() + others.length + 1);
        for (NutsTextNodeStyle i : elements) {
            all.add(i);
        }
        for (NutsTextNodeStyle i : others) {
            if (i != null) {
                all.add(i);
            }
        }
        if (all.isEmpty()) {
            return NONE;
        }
        return new NutsTextNodeStyles(all.toArray(new NutsTextNodeStyle[0]));
    }

    public NutsTextNodeStyles append(NutsTextNodeStyle other) {
        if (other == null) {
            return this;
        }
        NutsTextNodeStyle[] elements2 = new NutsTextNodeStyle[elements.length + 1];
        System.arraycopy(elements, 0, elements2, 0, elements.length);
        elements2[elements.length] = other;
        return new NutsTextNodeStyles(elements2);
    }

    public NutsTextNodeStyles removeLast() {
        if (elements.length <= 0) {
            return this;
        }
        return new NutsTextNodeStyles(Arrays.copyOf(elements, elements.length - 1));
    }

    public NutsTextNodeStyles removeFirst() {
        if (elements.length <= 0) {
            return this;
        }
        return new NutsTextNodeStyles(Arrays.copyOfRange(elements, 1,elements.length));
    }

    public NutsTextNodeStyle get(int index) {
        return elements[index];
    }

    public int size() {
        return elements.length;
    }

    @Override
    public String toString() {
        return Arrays.toString(elements);
    }

    public boolean isNone() {
        return elements.length == 0;
    }

    @Override
    public Iterator<NutsTextNodeStyle> iterator() {
        return Arrays.asList(elements).iterator();
    }


}

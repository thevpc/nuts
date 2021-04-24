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
public final class NutsTextStyles implements Iterable<NutsTextStyle> {

    public static NutsTextStyles NONE = new NutsTextStyles(new NutsTextStyle[0]);

    private NutsTextStyle[] elements;

    private NutsTextStyles(NutsTextStyle[] elements) {
        this.elements = Arrays.copyOf(elements, elements.length);
    }

    public static NutsTextStyles of(NutsTextStyle... others) {
        return NONE.append(others);
    }

    public static NutsTextStyles of(NutsTextStyle other) {
        if (other == null) {
            return NONE;
        }
        return new NutsTextStyles(new NutsTextStyle[]{other});
    }

    public NutsTextStyles append(NutsTextStyles other) {
        if (other == null || other.isNone()) {
            return this;
        }
        if (this.isNone()) {
            return other;
        }
        return append(other.elements);
    }

    public NutsTextStyles append(NutsTextStyle... others) {
        if (others.length == 0) {
            return this;
        }
        List<NutsTextStyle> all = new ArrayList<NutsTextStyle>(size() + others.length + 1);
        for (NutsTextStyle i : elements) {
            all.add(i);
        }
        for (NutsTextStyle i : others) {
            if (i != null) {
                all.add(i);
            }
        }
        if (all.isEmpty()) {
            return NONE;
        }
        return new NutsTextStyles(all.toArray(new NutsTextStyle[0]));
    }

    public NutsTextStyles append(NutsTextStyle other) {
        if (other == null) {
            return this;
        }
        NutsTextStyle[] elements2 = new NutsTextStyle[elements.length + 1];
        System.arraycopy(elements, 0, elements2, 0, elements.length);
        elements2[elements.length] = other;
        return new NutsTextStyles(elements2);
    }

    public NutsTextStyles removeLast() {
        if (elements.length <= 0) {
            return this;
        }
        return new NutsTextStyles(Arrays.copyOf(elements, elements.length - 1));
    }

    public NutsTextStyles removeFirst() {
        if (elements.length <= 0) {
            return this;
        }
        return new NutsTextStyles(Arrays.copyOfRange(elements, 1,elements.length));
    }

    public NutsTextStyle get(int index) {
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
    public Iterator<NutsTextStyle> iterator() {
        return Arrays.asList(elements).iterator();
    }


}

/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
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
package net.thevpc.nuts.text;

import net.thevpc.nuts.NutsEnum;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.util.NutsStringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public final class NutsTextStyles implements Iterable<NutsTextStyle>, NutsEnum {

    public static NutsTextStyles PLAIN = new NutsTextStyles(new NutsTextStyle[0]);

    private final NutsTextStyle[] elements;

    private NutsTextStyles(NutsTextStyle[] elements) {
        this.elements = Arrays.copyOf(elements, elements.length);
    }

    public static NutsTextStyles of(NutsTextStyle... others) {
        if (others==null || others.length==0) {
            return PLAIN;
        }
        Map<NutsTextStyleType, NutsTextStyle> visited = new TreeMap<>();
        for (NutsTextStyle element : others) {
            if (element != null) {
                visited.put(element.getType(), element);
            }
        }
        visited.remove(NutsTextStyleType.PLAIN);
        if (visited.isEmpty()) {
            return PLAIN;
        }
        return new NutsTextStyles(visited.values().toArray(new NutsTextStyle[0]));
    }

    public static NutsTextStyles of(NutsTextStyle other) {
        if (other == null || other.getType() == NutsTextStyleType.PLAIN) {
            return PLAIN;
        }
        return new NutsTextStyles(new NutsTextStyle[]{other});
    }


    public static NutsOptional<NutsTextStyles> parse(String value) {
        value = NutsStringUtils.trim(value);
        if (value.isEmpty()) {
            return NutsOptional.ofEmpty(s -> NutsMessage.ofCstyle("%s is empty",NutsTextStyles.class.getSimpleName()));
        }
        List<NutsTextStyle> all = new ArrayList<>();
        for (String s : value.split(",")) {
            s = s.trim();
            if (s.length() > 0) {
                NutsTextStyle a = NutsTextStyle.parse(s).orNull();
                if (a == null) {
                    String finalValue = value;
                    return NutsOptional.ofError(session -> NutsMessage.ofCstyle(NutsTextStyles.class.getSimpleName() + " invalid value : %s", finalValue));
                }
                all.add(a);
            }
        }
        return NutsOptional.of(of(all.toArray(new NutsTextStyle[0])));
    }

    public NutsTextStyles append(NutsTextStyles other) {
        if (other == null || other.isPlain()) {
            return this;
        }
        if (this.isPlain()) {
            return other;
        }
        return append(other.elements);
    }

    public NutsTextStyles append(NutsTextStyle... others) {
        if (others == null || others.length == 0) {
            return this;
        }
        List<NutsTextStyle> all = new ArrayList<NutsTextStyle>(size() + others.length + 1);
        all.addAll(Arrays.asList(elements));
        all.addAll(Arrays.asList(others));
        return of(all.toArray(new NutsTextStyle[0]));
    }

    public NutsTextStyles append(NutsTextStyle other) {
        if (other == null || other.getType() == NutsTextStyleType.PLAIN) {
            return this;
        }
        NutsTextStyle[] elements2 = new NutsTextStyle[elements.length + 1];
        System.arraycopy(elements, 0, elements2, 0, elements.length);
        elements2[elements.length] = other;
        return of(elements2);
    }

    public NutsTextStyles removeLast() {
        if (elements.length <= 0) {
            return this;
        }
        return of(Arrays.copyOf(elements, elements.length - 1));
    }

    public NutsTextStyles removeFirst() {
        if (elements.length <= 0) {
            return this;
        }
        return of(Arrays.copyOfRange(elements, 1, elements.length));
    }

    public NutsTextStyle get(int index) {
        return elements[index];
    }

    public int size() {
        return elements.length;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(elements);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsTextStyles that = (NutsTextStyles) o;
        return Arrays.equals(elements, that.elements);
    }

    @Override
    public String toString() {
        return id();
    }

    public boolean isPlain() {
        return elements.length == 0;
    }

    @Override
    public Iterator<NutsTextStyle> iterator() {
        return Arrays.asList(elements).iterator();
    }

    public NutsTextStyle[] toArray() {
        return Arrays.copyOf(elements, elements.length);
    }

    public List<NutsTextStyle> toList() {
        return Collections.unmodifiableList(Arrays.asList(elements));
    }


    @Override
    public String id() {
        if (elements.length == 0) {
            return "plain";
        }
        return Arrays.stream(elements).map(NutsTextStyle::id).collect(Collectors.joining(","));
    }
}

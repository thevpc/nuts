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

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public final class NTextStyles implements Iterable<NTextStyle>, NEnum {

    public static NTextStyles PLAIN = new NTextStyles(new NTextStyle[0]);

    private final NTextStyle[] elements;

    private NTextStyles(NTextStyle[] elements) {
        this.elements = Arrays.copyOf(elements, elements.length);
    }

    public static NTextStyles of(NTextStyle... others) {
        if (others == null || others.length == 0) {
            return PLAIN;
        }
        Map<NTextStyleType, NTextStyle> visited = new TreeMap<>();
        for (NTextStyle element : others) {
            if (element != null) {
                visited.put(element.getType(), element);
            }
        }
        visited.remove(NTextStyleType.PLAIN);
        if (visited.isEmpty()) {
            return PLAIN;
        }
        return new NTextStyles(visited.values().toArray(new NTextStyle[0]));
    }

    public static NTextStyles of(NTextStyle other) {
        if (other == null || other.getType() == NTextStyleType.PLAIN) {
            return PLAIN;
        }
        return new NTextStyles(new NTextStyle[]{other});
    }


    public static NOptional<NTextStyles> parse(String value) {
        value = NStringUtils.trim(value);
        if (value.isEmpty()) {
            return NOptional.ofEmpty(s -> NMsg.ofC("%s is empty", NTextStyles.class.getSimpleName()));
        }
        List<NTextStyle> all = new ArrayList<>();
        for (String s : NStringUtils.split(value, ",", true, true)) {
            s = s.trim();
            if (s.length() > 0) {
                NTextStyle a = NTextStyle.parse(s).orNull();
                if (a == null) {
                    String finalValue = value;
                    return NOptional.ofError(session -> NMsg.ofC("%s invalid value : %s", NTextStyles.class.getSimpleName(), finalValue));
                }
                all.add(a);
            }
        }
        return NOptional.of(of(all.toArray(new NTextStyle[0])));
    }

    public NTextStyles append(NTextStyles other) {
        if (other == null || other.isPlain()) {
            return this;
        }
        if (this.isPlain()) {
            return other;
        }
        return append(other.elements);
    }

    public NTextStyles append(NTextStyle... others) {
        if (others == null || others.length == 0) {
            return this;
        }
        List<NTextStyle> all = new ArrayList<NTextStyle>(size() + others.length + 1);
        all.addAll(Arrays.asList(elements));
        all.addAll(Arrays.asList(others));
        return of(all.toArray(new NTextStyle[0]));
    }

    public NTextStyles append(NTextStyle other) {
        if (other == null || other.getType() == NTextStyleType.PLAIN) {
            return this;
        }
        NTextStyle[] elements2 = new NTextStyle[elements.length + 1];
        System.arraycopy(elements, 0, elements2, 0, elements.length);
        elements2[elements.length] = other;
        return of(elements2);
    }

    public NTextStyles removeLast() {
        if (elements.length <= 0) {
            return this;
        }
        return of(Arrays.copyOf(elements, elements.length - 1));
    }

    public NTextStyles removeFirst() {
        if (elements.length <= 0) {
            return this;
        }
        return of(Arrays.copyOfRange(elements, 1, elements.length));
    }

    public NTextStyle get(int index) {
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
        NTextStyles that = (NTextStyles) o;
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
    public Iterator<NTextStyle> iterator() {
        return Arrays.asList(elements).iterator();
    }

    public NTextStyle[] toArray() {
        return Arrays.copyOf(elements, elements.length);
    }

    public List<NTextStyle> toList() {
        return Collections.unmodifiableList(Arrays.asList(elements));
    }


    @Override
    public String id() {
        if (elements.length == 0) {
            return "plain";
        }
        return Arrays.stream(elements).map(NTextStyle::id).collect(Collectors.joining(","));
    }
}

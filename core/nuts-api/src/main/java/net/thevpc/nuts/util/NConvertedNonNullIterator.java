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

import net.thevpc.nuts.elem.NDescribableElementSupplier;
import net.thevpc.nuts.elem.NElement;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Created by vpc on 1/9/17.
 *
 * @param <F> From Type
 * @param <T> To Type
 */
public class NConvertedNonNullIterator<F, T> extends NIteratorBase<T> {

    private final Iterator<F> base;
    private final Function<F, T> converter;
    private final String convertName;
    private T lastVal;

    public NConvertedNonNullIterator(Iterator<F> base, Function<F, T> converter, String convertName) {
        this.base = base;
        this.converter = converter;
        if (convertName == null) {
            convertName = this.converter.toString();
        }
        this.convertName = convertName;
    }

    @Override
    public NElement describe() {
        return NElement.ofObjectBuilder()
                .name("Map")
                .set("accept", "isNotNull")
                .set("mapper", NDescribableElementSupplier.describeResolveOrDestructAsObject(converter)
                        .builder()
                        .set("name", convertName)
                        .build()
                )
                .build();
    }

    @Override
    public boolean hasNext() {
        while (base.hasNext()) {
            F i = base.next();
            if (i != null) {
                lastVal = converter.apply(i);
                if (lastVal != null) {
                    break;
                }
            }
        }
        return lastVal != null;
    }

    @Override
    public T next() {
        return lastVal;
    }

    @Override
    public void remove() {
        throw new IllegalArgumentException("unsupported remove");
    }

    @Override
    public String toString() {
        return "ConvertedNonNullIterator(" + base + "," + convertName + ")";
    }
}

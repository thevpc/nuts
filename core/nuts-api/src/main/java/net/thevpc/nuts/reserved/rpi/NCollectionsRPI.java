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
package net.thevpc.nuts.reserved.rpi;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NIterable;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStream;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Collections related Internal Programming Interface
 */
public interface NCollectionsRPI extends NComponent {
    static NCollectionsRPI of() {
        return NExtensions.of(NCollectionsRPI.class);
    }

    <T> NStream<T> arrayToStream(T[] str);

    <T> NStream<T> iterableToStream(Iterable<T> str);

    <T> NStream<T> iteratorToStream(Iterator<T> str);

    <T> NStream<T> toStream(Stream<T> str);

    <T> NStream<T> emptyStream();

    <T> NIterator<T> emptyIterator();

    <T> NIterator<T> toIterator(Iterator<T> str);

    <T> NIterable<T> toIterable(Iterable<T> str);

    <T> NStream<T> optionalToStream(Optional<T> str);

    <T> NStream<T> optionalToStream(NOptional<T> str);
}

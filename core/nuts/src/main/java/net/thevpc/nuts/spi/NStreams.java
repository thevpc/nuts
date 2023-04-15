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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NIterable;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NStream;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

public interface NStreams extends NComponent {
    static NStreams of(NSession session) {
        return session.extensions().createComponent(NStreams.class).get();
    }

    <T> NStream<T> createStream(T[] str, Function<NSession, NElement> name);

    <T> NStream<T> createStream(Iterable<T> str, Function<NSession, NElement> name);

    <T> NStream<T> createStream(Iterator<T> str, Function<NSession, NElement> name);

    <T> NStream<T> createStream(NIterable<T> str);
    <T> NStream<T> createStream(NIterator<T> str);

    <T> NStream<T> createStream(Stream<T> str, Function<NSession, NElement> name);

    <T> NStream<T> createEmptyStream();
}

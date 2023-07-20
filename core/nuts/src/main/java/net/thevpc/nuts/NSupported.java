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
package net.thevpc.nuts;

import net.thevpc.nuts.util.NAssert;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface NSupported<T> {
    /**
     * minimum support level for user defined implementations.
     */
    int CUSTOM_SUPPORT = 1000;
    /**
     * this is the default support level for runtime implementation (nuts-runtime).
     */
    int DEFAULT_SUPPORT = 10;
    /**
     * when getSupportLevel(...)==NO_SUPPORT the package is discarded.
     */
    int NO_SUPPORT = -1;

    static <T> NSupported<T> resolve(Collection<Supplier<NSupported<T>>> source, Function<NSession, NMsg> emptyMessage) {
        if (source == null) {
            return invalid(emptyMessage);
        }
        return resolve(source.stream(),emptyMessage);
    }

    static <T> NSupported<T> resolve(Stream<Supplier<NSupported<T>>> source, Function<NSession, NMsg> emptyMessage) {
        Object[] track = new Object[2];
        if (source != null) {
            source.forEach(i -> {
                NSupported<T> s = i.get();
                NAssert.requireNonNull(s, "NSupported<T>");
                int supportLevel = s.getSupportLevel();
                boolean valid = supportLevel > 0;
                if (valid) {
                    if (track[0] == null) {
                        track[0] = s;
                        track[1] = supportLevel;
                    } else {
                        int oldSupportLevel = (Integer) track[1];
                        if (supportLevel > oldSupportLevel) {
                            track[0] = s;
                            track[1] = supportLevel;
                        }
                    }
                }
            });
        }
        NSupported<T> r = (NSupported<T>) track[0];
        if (r == null) {
            return invalid(emptyMessage);
        }
        return (NSupported<T>) r;
    }

    static <T> NSupported<T> of(int supportLevel, T value) {
        return of(supportLevel,value,null);
    }

    static <T> NSupported<T> of(int supportLevel, T value, Function<NSession, NMsg> emptyMessage) {
        return supportLevel <= 0 ? invalid(emptyMessage) : new DefaultNSupported<>(() -> value, supportLevel,emptyMessage);
    }

    static <T> NSupported<T> of(int supportLevel, Supplier<T> supplier) {
        return of(supportLevel,supplier,null);
    }

    static <T> NSupported<T> of(int supportLevel, Supplier<T> supplier, Function<NSession, NMsg> emptyMessage) {
        return (supportLevel <= 0 || supplier == null) ? invalid(emptyMessage)
                : new DefaultNSupported<>(supplier, supportLevel,emptyMessage)
                ;
    }

    @SuppressWarnings("unchecked")
    static <T> NSupported<T> invalid(Function<NSession, NMsg> emptyMessage) {
        return new DefaultNSupported<>(null, -1,emptyMessage);
    }

    static <T> boolean isValid(NSupported<T> s) {
        return s != null && s.isValid();
    }

    T getValue();

    default boolean isValid() {
        return getSupportLevel() > 0;
    }

    int getSupportLevel();

    NOptional<T> toOptional();
}

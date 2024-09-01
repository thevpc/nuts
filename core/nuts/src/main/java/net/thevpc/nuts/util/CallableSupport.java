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
package net.thevpc.nuts.util;

import net.thevpc.nuts.NConstants;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface CallableSupport<T> {

    static <T> CallableSupport<T> resolve(Collection<CallableSupport<T>> source, Supplier<String> emptyMessage) {
        if (source == null) {
            return invalid(emptyMessage);
        }
        return resolve(source.stream(), emptyMessage);
    }

    static <T> CallableSupport<T> resolve(Stream<CallableSupport<T>> source, Supplier<String> emptyMessage) {
        if (source == null) {
            return invalid(emptyMessage);
        }
        return resolveSupplier(source.map(x -> () -> x), emptyMessage);
    }

    static <T> CallableSupport<T> resolveSupplier(Collection<Supplier<CallableSupport<T>>> source, Supplier<String> emptyMessage) {
        if (source == null) {
            return invalid(emptyMessage);
        }
        return resolveSupplier(source.stream(), emptyMessage);
    }

    static <T> CallableSupport<T> resolveSupplier(Stream<Supplier<CallableSupport<T>>> source, Supplier<String> emptyMessage) {
        Object[] track = new Object[2];
        if (source != null) {
            source.forEach(i -> {
                CallableSupport<T> s = i.get();
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
        CallableSupport<T> r = (CallableSupport<T>) track[0];
        if (r == null) {
            return invalid(emptyMessage);
        }
        return (CallableSupport<T>) r;
    }

    static <T> CallableSupport<T> of(int supportLevel, T value) {
        return of(supportLevel, value, null);
    }

    static <T> CallableSupport<T> of(int supportLevel, T value, Supplier<String> emptyMessage) {
        return supportLevel <= 0 ? invalid(emptyMessage) : new DefaultCallableSupport<>(() -> value, supportLevel, emptyMessage);
    }

    static <T> CallableSupport<T> of(int supportLevel, Supplier<T> supplier) {
        return of(supportLevel, supplier, null);
    }

    static <T> CallableSupport<T> of(int supportLevel, Supplier<T> supplier, Supplier<String> emptyMessage) {
        return (supportLevel <= 0 || supplier == null) ? invalid(emptyMessage)
                : new DefaultCallableSupport<>(supplier, supportLevel, emptyMessage)
                ;
    }

    @SuppressWarnings("unchecked")
    static <T> CallableSupport<T> invalid(Supplier<String> emptyMessage) {
        return new DefaultCallableSupport<>(null, NConstants.Support.NO_SUPPORT, emptyMessage);
    }

    static <T> boolean isValid(CallableSupport<T> s) {
        return s != null && s.isValid();
    }

    T call();

    default boolean isValid() {
        return getSupportLevel() > 0;
    }

    int getSupportLevel();

    NOptional<T> toOptional();
}

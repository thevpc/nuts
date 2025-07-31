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
package net.thevpc.nuts;

import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface NCallableSupport<T> {
    static <T> NCallableSupport<T> resolveNamed(Collection<NCallableSupport<T>> source, String name) {
        return resolveNamed(source, name == null ? null : NMsg.ofC("could not resolve %s", name));
    }

    static <T> NCallableSupport<T> resolve(Collection<NCallableSupport<T>> source, NMsg emptyMessage) {
        if (source == null) {
            return ofInvalid(emptyMessage);
        }
        return resolve(source.stream(), emptyMessage == null ? null : () -> emptyMessage);
    }

    static <T> NCallableSupport<T> resolveNamed(Collection<NCallableSupport<T>> source, NMsg name) {
        if (source == null) {
            return ofInvalid(NMsg.ofC("could not resolve %s", name));
        }
        return resolve(source.stream(), name == null ? null : () -> NMsg.ofC("could not resolve %s", name));
    }

    static <T> NCallableSupport<T> resolve(Collection<NCallableSupport<T>> source, Supplier<NMsg> emptyMessage) {
        if (source == null) {
            return ofInvalid(emptyMessage);
        }
        return resolve(source.stream(), emptyMessage);
    }

    static <T> NCallableSupport<T> resolve(Stream<NCallableSupport<T>> source, Supplier<NMsg> emptyMessage) {
        if (source == null) {
            return ofInvalid(emptyMessage);
        }
        return resolveSupplier(source.map(x -> () -> x), emptyMessage);
    }

    static <T> NCallableSupport<T> resolveNamed(Stream<NCallableSupport<T>> source, String name) {
        return resolveNamed(source, name == null ? null : NMsg.ofC("could not resolve %s", name));
    }

    static <T> NCallableSupport<T> resolve(Stream<NCallableSupport<T>> source, NMsg emptyMessage) {
        if (source == null) {
            return ofInvalid(emptyMessage);
        }
        return resolve(source, emptyMessage == null ? null : () -> emptyMessage);
    }

    static <T> NCallableSupport<T> resolveNamed(Stream<NCallableSupport<T>> source, NMsg name) {
        if (source == null) {
            return ofInvalid(NMsg.ofC("could not resolve %s", name));
        }
        return resolve(source, name == null ? null : () -> NMsg.ofC("could not resolve %s", name));
    }


    static <T> NCallableSupport<T> resolveSupplier(Collection<Supplier<NCallableSupport<T>>> source, Supplier<NMsg> emptyMessage) {
        if (source == null) {
            return ofInvalid(emptyMessage);
        }
        return resolveSupplier(source.stream(), emptyMessage);
    }

    static <T> NCallableSupport<T> resolveSupplier(Stream<Supplier<NCallableSupport<T>>> source, Supplier<NMsg> emptyMessage) {
        Object[] track = new Object[2];
        if (source != null) {
            source.forEach(i -> {
                NCallableSupport<T> s = i.get();
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
        NCallableSupport<T> r = (NCallableSupport<T>) track[0];
        if (r == null) {
            return ofInvalid(emptyMessage);
        }
        return (NCallableSupport<T>) r;
    }

    static <T> NCallableSupport<T> of(int supportLevel, T value) {
        return of(supportLevel, value, null);
    }

    static <T> NCallableSupport<T> of(int supportLevel, T value, Supplier<NMsg> emptyMessage) {
        return supportLevel <= 0 ? ofInvalid(emptyMessage) : new DefaultNCallableSupport<>(() -> value, supportLevel, emptyMessage);
    }

    static <T> NCallableSupport<T> of(int supportLevel, Supplier<T> supplier) {
        return of(supportLevel, supplier, null);
    }

    static <T> NCallableSupport<T> of(int supportLevel, Supplier<T> supplier, Supplier<NMsg> emptyMessage) {
        return (supportLevel <= 0 || supplier == null) ? ofInvalid(emptyMessage)
                : new DefaultNCallableSupport<>(supplier, supportLevel, emptyMessage)
                ;
    }

    static <T> NCallableSupport<T> ofValid(T value) {
        return of(NConstants.Support.DEFAULT_SUPPORT, value, null);
    }

    static <T> NCallableSupport<T> ofValid(Supplier<T> supplier) {
        return of(NConstants.Support.DEFAULT_SUPPORT, supplier, null);
    }

    static <T> NCallableSupport<T> ofValid(int supportLevel,T value) {
        return of((supportLevel<=0)?NConstants.Support.DEFAULT_SUPPORT:supportLevel, value, null);
    }

    static <T> NCallableSupport<T> ofValid(int supportLevel,Supplier<T> supplier) {
        return of((supportLevel<=0)?NConstants.Support.DEFAULT_SUPPORT:supportLevel, supplier, null);
    }

    @SuppressWarnings("unchecked")
    static <T> NCallableSupport<T> ofInvalid(Supplier<NMsg> emptyMessage) {
        return new DefaultNCallableSupport<>(null, NConstants.Support.NO_SUPPORT, emptyMessage);
    }

    @SuppressWarnings("unchecked")
    static <T> NCallableSupport<T> ofInvalid(NMsg emptyMessage) {
        return new DefaultNCallableSupport<>(null, NConstants.Support.NO_SUPPORT, emptyMessage == null ? null : () -> emptyMessage);
    }

    static <T> boolean isValid(NCallableSupport<T> s) {
        return s != null && s.isValid();
    }

    T call();

    default boolean isValid() {
        return getSupportLevel() > 0;
    }

    int getSupportLevel();

    NOptional<T> toOptional();
}

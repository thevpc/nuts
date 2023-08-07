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

public interface NRunnableSupport {
    static NRunnableSupport resolve(Collection<NRunnableSupport> source, Function<NSession, NMsg> emptyMessage) {
        if (source == null) {
            return invalid(emptyMessage);
        }
        return resolve(source.stream(), emptyMessage);
    }

    static NRunnableSupport resolve(Stream<NRunnableSupport> source, Function<NSession, NMsg> emptyMessage) {
        if (source == null) {
            return invalid(emptyMessage);
        }
        return resolveSupplier(source.map(x -> () -> x), emptyMessage);
    }

    static NRunnableSupport resolveSupplier(Collection<Supplier<NRunnableSupport>> source, Function<NSession, NMsg> emptyMessage) {
        if (source == null) {
            return invalid(emptyMessage);
        }
        return resolveSupplier(source.stream(), emptyMessage);
    }

    static NRunnableSupport resolveSupplier(Stream<Supplier<NRunnableSupport>> source, Function<NSession, NMsg> emptyMessage) {
        Object[] track = new Object[2];
        if (source != null) {
            source.forEach(i -> {
                NRunnableSupport s = i.get();
                NAssert.requireNonNull(s, "NRunnableSupport");
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
        NRunnableSupport r = (NRunnableSupport) track[0];
        if (r == null) {
            return invalid(emptyMessage);
        }
        return (NRunnableSupport) r;
    }

    static NRunnableSupport of(int supportLevel, Runnable supplier) {
        return of(supportLevel, supplier, null);
    }

    static NRunnableSupport of(int supportLevel, Runnable supplier, Function<NSession, NMsg> emptyMessage) {
        return (supportLevel <= 0 || supplier == null) ? invalid(emptyMessage)
                : new DefaultNRunnableSupport(supplier, supportLevel, emptyMessage)
                ;
    }

    @SuppressWarnings("unchecked")
    static NRunnableSupport invalid(Function<NSession, NMsg> emptyMessage) {
        return new DefaultNRunnableSupport(null, NConstants.Support.NO_SUPPORT, emptyMessage);
    }

    static boolean isValid(NRunnableSupport s) {
        return s != null && s.isValid();
    }

    void run(NSession session);

    default boolean isValid() {
        return getSupportLevel() > 0;
    }

    int getSupportLevel();
}

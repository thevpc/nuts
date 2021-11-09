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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default Logging verb names
 *
 * @app.category Logging
 */
public final class NutsLogVerb {
    private static final Map<String, NutsLogVerb> cached = new HashMap<>();

    public static final NutsLogVerb INFO = of("INFO");
    public static final NutsLogVerb DEBUG = of("DEBUG");

    /**
     * Log verb used for tracing the start of an operation
     */
    public static final NutsLogVerb START = of("START");

    /**
     * Log verb used for tracing the successful termination of an operation
     */
    public static final NutsLogVerb SUCCESS = of("SUCCESS");

    /**
     * Log verb used for tracing general purpose warnings
     */
    public static final NutsLogVerb WARNING = of("WARNING");

    /**
     * Log verb used for tracing the failure to run an operation
     */
    public static final NutsLogVerb FAIL = of("FAIL");

    /**
     * Log verb used for tracing a I/O read operation
     */
    public static final NutsLogVerb READ = of("READ");

    public static final NutsLogVerb UPDATE = of("UPDATE");

    /**
     * Log verb used for tracing cache related operations
     */
    public static final NutsLogVerb CACHE = of("CACHE");

    private final String name;

    public NutsLogVerb(String name) {
        if (name == null) {
            throw new NullPointerException("null log verb");
        }
        this.name = name;
    }

    public static NutsLogVerb of(String name) {
        NutsLogVerb t = cached.get(name);
        if (t == null) {
            synchronized (cached) {
                t = cached.get(name);
                if (t == null) {
                    cached.put(name, t = new NutsLogVerb(name));
                }
            }
        }
        return t;
    }

    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsLogVerb that = (NutsLogVerb) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public String toString() {
        return name;
    }
}

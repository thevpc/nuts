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
package net.thevpc.nuts.log;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default Logging verb names
 *
 * @app.category Logging
 */
public final class NLogVerb {
    private static final Map<String, NLogVerb> cached = new HashMap<>();

    public static final NLogVerb INFO = of("INFO");
    public static final NLogVerb DEBUG = of("DEBUG");

    /**
     * Log verb used for tracing the start of an operation
     */
    public static final NLogVerb START = of("START");

    /**
     * Log verb used for tracing the successful termination of an operation
     */
    public static final NLogVerb SUCCESS = of("SUCCESS");

    /**
     * Log verb used for tracing general purpose warnings
     */
    public static final NLogVerb WARNING = of("WARNING");

    /**
     * Log verb used for tracing the failure to run an operation
     */
    public static final NLogVerb FAIL = of("FAIL");

    /**
     * Log verb used for tracing a I/O read operation
     */
    public static final NLogVerb READ = of("READ");

    public static final NLogVerb UPDATE = of("UPDATE");
    public static final NLogVerb ADD = of("ADD");
    public static final NLogVerb REMOVE = of("REMOVE");

    /**
     * Log verb used for tracing cache related operations
     */
    public static final NLogVerb CACHE = of("CACHE");
    public static final NLogVerb PROGRESS = of("PROGRESS");

    private final String name;

    public NLogVerb(String name) {
        if (name == null) {
            throw new NullPointerException("null log verb");
        }
        this.name = name;
    }

    public static NLogVerb of(String name) {
        NLogVerb t = cached.get(name);
        if (t == null) {
            synchronized (cached) {
                t = cached.get(name);
                if (t == null) {
                    cached.put(name, t = new NLogVerb(name));
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
        NLogVerb that = (NLogVerb) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public String toString() {
        return name;
    }
}

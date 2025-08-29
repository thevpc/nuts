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

import net.thevpc.nuts.util.NAssert;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default Logging verb names
 *
 * @app.category Logging
 */
public final class NMsgIntent {
    private static final Map<String, NMsgIntent> cached = new HashMap<>();

    public static final NMsgIntent INFO = of("INFO");
    public static final NMsgIntent DEBUG = of("DEBUG");

    /**
     * Log verb used for tracing the start of an operation
     */
    public static final NMsgIntent START = of("START");

    /**
     * Log verb used for tracing the successful termination of an operation
     */
    public static final NMsgIntent SUCCESS = of("SUCCESS");

    /**
     * Log verb used for tracing general purpose warnings
     */
    public static final NMsgIntent ALERT = of("WARNING");

    /**
     * Log verb used for tracing general purpose warnings
     */
    public static final NMsgIntent CONFIG = of("CONFIG");

    /**
     * Log verb used for tracing the failure to run an operation
     */
    public static final NMsgIntent FAIL = of("FAIL");

    /**
     * Log verb used for tracing a I/O read operation
     */
    public static final NMsgIntent READ = of("READ");

    public static final NMsgIntent UPDATE = of("UPDATE");
    public static final NMsgIntent ADD = of("ADD");
    public static final NMsgIntent REMOVE = of("REMOVE");

    /**
     * Log verb used for tracing cache related operations
     */
    public static final NMsgIntent CACHE = of("CACHE");
    public static final NMsgIntent PROGRESS = of("PROGRESS");

    private final String name;

    public NMsgIntent(String name) {
        NAssert.requireNonBlank(name, "log verb");
        this.name = name;
    }

    public static NMsgIntent of(String name) {
        NMsgIntent t = cached.get(name);
        if (t == null) {
            synchronized (cached) {
                t = cached.get(name);
                if (t == null) {
                    cached.put(name, t = new NMsgIntent(name));
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
        NMsgIntent that = (NMsgIntent) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public String toString() {
        return name;
    }
}

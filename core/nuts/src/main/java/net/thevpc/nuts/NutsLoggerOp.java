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

import net.thevpc.nuts.boot.NutsApiUtils;

import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Log operation
 * @app.category Logging
 */
public interface NutsLoggerOp {
    static NutsLoggerOp of(Class clazz, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().log().of(clazz).with();
    }

    NutsLoggerOp session(NutsSession session);

    /**
     * set or unset formatted mode (Nuts Stream Format)
     * @param value formatted flag
     * @return {@code this} instance
     */
    NutsLoggerOp formatted(boolean value);

    /**
     * set formatted mode (Nuts Stream Format)
     * @return {@code this} instance
     */
    NutsLoggerOp formatted();

    /**
     * set log verb
     * @param verb verb or category
     * @return {@code this} instance
     */
    NutsLoggerOp verb(NutsLogVerb verb);

    /**
     * set log error
     * @param error error thrown
     * @return {@code this} instance
     */
    NutsLoggerOp error(Throwable error);

    /**
     * set operation time
     * @param time operation time in ms
     * @return {@code this} instance
     */
    NutsLoggerOp time(long time);

    /**
     * set operation level
     * @param level message level
     * @return {@code this} instance
     */
    NutsLoggerOp level(Level level);

    /**
     * set message style (cstyle or jstyle)
     * @param style message format style
     * @return {@code this} instance
     */
    NutsLoggerOp style(NutsTextFormatStyle style);

    /**
     * log the given message
     * @param msg message
     * @param params message params
     */
    void log(String msg, Object... params);

    /**
     * log the given message
     * @param msgSupplier message supplier
     */
    void log(Supplier<String> msgSupplier);
}

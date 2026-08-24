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
package net.thevpc.nuts.io;

import net.thevpc.nuts.internal.rpi.NIORPI;

/**
 * NMemoryPrintStream interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NMemoryPrintStream extends NPrintStream {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NMemoryPrintStream of() {
        return NIORPI.of().createInMemoryPrintStream();
    }

    /**
     * Creates a new instance of of.
     *
     * @param mode mode
     * @return of result
     */
    static NMemoryPrintStream of(NTerminalMode mode) {
        return NIORPI.of().createInMemoryPrintStream(mode);
    }

    /**
     * Bytes.
     *
     * @return bytes result
     */
    byte[] bytes();

    /**
     * As input source.
     *
     * @return as input source result
     */
    NInputSource asInputSource();

    @Override
    default void close() {
    }
}

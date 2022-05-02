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
package net.thevpc.nuts.io;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

import java.io.OutputStream;
import java.io.Writer;

public interface NutsPrintStreams extends NutsComponent {
    static NutsPrintStreams of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsPrintStreams.class, true, session);
    }


    NutsPrintStream createNull();

    NutsMemoryPrintStream createInMemory();

    /**
     * create print stream that supports the given {@code mode}. If the given
     * {@code out} is a PrintStream that supports {@code mode}, it should be
     * returned without modification.
     *
     * @param out  stream to wrap
     * @param mode mode to support
     * @param terminal terminal
     * @return {@code mode} supporting PrintStream
     */
    NutsPrintStream create(OutputStream out, NutsTerminalMode mode, NutsSystemTerminalBase terminal);

    NutsPrintStream create(OutputStream out);

    NutsPrintStream create(Writer out, NutsTerminalMode mode, NutsSystemTerminalBase terminal);

    NutsPrintStream create(Writer out);

    boolean isStdout(NutsPrintStream out);

    boolean isStderr(NutsPrintStream out);

    NutsPrintStream stdout();

    NutsPrintStream stderr();

}

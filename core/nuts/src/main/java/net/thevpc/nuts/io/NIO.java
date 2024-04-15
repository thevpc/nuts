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

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NSystemTerminalBase;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

public interface NIO extends NComponent {
    static NIO of(NSession session) {
        return NExtensions.of(session).createComponent(NIO.class).get();
    }

    InputStream ofNullRawInputStream();

    boolean isStdin(InputStream in);

    InputStream stdin();

    OutputStream ofNullRawOutputStream();

    boolean isStdout(NPrintStream out);

    boolean isStderr(NPrintStream out);

    NPrintStream stdout();

    NPrintStream stderr();

    /**
     * return workspace system terminal.
     *
     * @return workspace system terminal
     */
    NSystemTerminal getSystemTerminal();

    /**
     * update workspace wide system terminal
     *
     * @param terminal system terminal
     * @return {@code this} instance
     */
    NIO setSystemTerminal(NSystemTerminalBase terminal);

    /**
     * return workspace default terminal
     *
     * @return workspace default terminal
     */
    NSessionTerminal getDefaultTerminal();

    /**
     * update workspace wide terminal
     *
     * @param terminal terminal
     * @return {@code this} instance
     */
    NIO setDefaultTerminal(NSessionTerminal terminal);


}

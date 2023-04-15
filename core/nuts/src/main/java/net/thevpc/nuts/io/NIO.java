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

import net.thevpc.nuts.NExtensions;
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

    static InputStream ofNullInputStream(NSession session) {
        return of(session).ofNullInputStream();
    }
    static NPrintStream ofNullPrintStream(NSession session) {
        return of(session).ofNullPrintStream();
    }

    InputStream ofNullInputStream();

    boolean isStdin(InputStream in);

    InputStream stdin();

    NPrintStream ofNullPrintStream();

    NMemoryPrintStream ofInMemoryPrintStream();

    NMemoryPrintStream ofInMemoryPrintStream(NTerminalMode mode);

    /**
     * create print stream that supports the given {@code mode}. If the given
     * {@code out} is a PrintStream that supports {@code mode}, it should be
     * returned without modification.
     *
     * @param out      stream to wrap
     * @param mode     mode to support
     * @param terminal terminal
     * @return {@code mode} supporting PrintStream
     */
    NPrintStream ofPrintStream(OutputStream out, NTerminalMode mode, NSystemTerminalBase terminal);

    NPrintStream ofPrintStream(OutputStream out);

    NPrintStream ofPrintStream(Writer out, NTerminalMode mode, NSystemTerminalBase terminal);

    NPrintStream ofPrintStream(Writer out);

    boolean isStdout(NPrintStream out);

    boolean isStderr(NPrintStream out);

    NPrintStream stdout();

    NPrintStream stderr();

    NInputSource ofMultiRead(NInputSource source);

    NInputSource ofInputSource(InputStream inputStream);
    NInputSource ofInputSource(InputStream inputStream, NInputSourceMetadata metadata);
    NInputSource ofInputSource(byte[] inputStream);

    NInputSource ofInputSource(byte[] inputStream, NInputSourceMetadata metadata);

    NOutputTarget ofOutputTarget(OutputStream inputStream);

    NOutputTarget ofOutputTarget(OutputStream inputStream, NOutputTargetMetadata metadata);
}

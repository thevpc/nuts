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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NExtensions;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NSessionTerminal;

import java.io.InputStream;

/**
 * This interface exposes utility methods to manipulate Terminals
 *
 * @app.category Input Output
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public interface NTerminals extends NComponent {

    /**
     * retrieves the singleton instance of NutsTerminals for the given session
     *
     * @param session session
     * @return the singleton instance of NutsTerminals for the given session
     */
    static NTerminals of(NSession session) {
       return NExtensions.of(session).createSupported(NTerminals.class);
    }

    /**
     * Checks for the current system terminal and does best effort
     * to enable a rich terminal. Rich terminals add somme features
     * including 'auto-complete'. This Method may replace the system
     * terminal and may even load a nuts extension to enable such features.
     *
     * @param session session
     * @return {@code this} instance
     */
    NTerminals enableRichTerm(NSession session);

    /**
     * return new terminal bound to the given session
     *
     * @param session session
     * @return new terminal
     */
    NSessionTerminal createTerminal(NSession session);

    /**
     * return new terminal
     *
     * @param in  in
     * @param out out
     * @param err err
     * @param session session
     * @return new terminal
     */
    NSessionTerminal createTerminal(InputStream in, NOutStream out, NOutStream err, NSession session);

    /**
     * return new terminal bound to the given parent terminal and session.
     *
     * @param terminal parent terminal (or null)
     * @param session  session
     * @return new terminal bound to the given parent terminal and session.
     */
    NSessionTerminal createTerminal(NSessionTerminal terminal, NSession session);

    /**
     * return a new terminal with empty input and byte-array output/error.
     * Using such terminals help capturing all output/error stream upon execution.
     * This method is equivalent to createMemTerminal(false,session)
     *
     * @param session session
     * @return a new terminal with empty input and byte-array output/error.
     */
    NSessionTerminal createMemTerminal(NSession session);

    /**
     * return a new terminal with empty input and byte-array output/error.
     * Using such terminals help capturing all output/error stream upon execution.
     *
     * @param mergeErr when true out and err are merged into a single stream
     * @param session  session
     * @return a new terminal with empty input and byte-array output/error.
     */
    NSessionTerminal createMemTerminal(boolean mergeErr, NSession session);

}

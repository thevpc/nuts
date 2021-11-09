/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.spi;

import net.thevpc.nuts.*;

import java.io.InputStream;

/**
 * System Terminal defines all low level term interaction
 *
 * @author thevpc
 * @app.category SPI Base
 * @since 0.5.4
 */
public interface NutsSystemTerminalBase extends NutsComponent {
    String readLine(NutsPrintStream out, NutsMessage message, NutsSession session);

    char[] readPassword(NutsPrintStream out, NutsMessage message, NutsSession session);

    InputStream getIn();

    NutsPrintStream getOut();

    NutsPrintStream getErr();

    default NutsCommandAutoCompleteResolver getAutoCompleteResolver() {
        return null;
    }

    default boolean isAutoCompleteSupported() {
        return false;
    }

    NutsSystemTerminalBase setCommandAutoCompleteResolver(NutsCommandAutoCompleteResolver autoCompleteResolver);

    /**
     * return History implementation
     *
     * @return History implementation
     */
    NutsCommandHistory getCommandHistory();

    /**
     * set History implementation
     *
     * @param history new history implementation
     * @return {@code this} instance
     */
    NutsSystemTerminalBase setCommandHistory(NutsCommandHistory history);

    /**
     * return command line language content type (or simple id) used for highlighting (syntax coloring).
     * when this returns blank, nuts uses 'system' which refers to the system shell highlighter
     *
     * @return command line language content type (or simple id) used for highlighting (syntax coloring)
     * @since 0.8.3
     */
    String getCommandHighlighter();

    /**
     * set command line language content type (or simple id) used for highlighting (syntax coloring).
     * when {@code commandContentType} is blank, nuts uses 'system' which refers to the system shell highlighter
     *
     * @param commandHighlighter commandContentType
     * @return {@code this} instance
     * @since 0.8.3
     */
    NutsSystemTerminalBase setCommandHighlighter(String commandHighlighter);


}

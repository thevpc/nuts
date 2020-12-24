/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
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

import java.util.List;

/**
 * Class responsible of formatting a formatted string.
 *
 * @author thevpc
 * @since 0.5.5
 * @category Format
 */
public interface NutsStringFormat extends NutsFormat {

    NutsTextFormatStyle getStyle();

    NutsStringFormat style(NutsTextFormatStyle style);

    NutsStringFormat setStyle(NutsTextFormatStyle style);

    Object[] getParameters();

    NutsStringFormat addParameters(Object... parameters);

    NutsStringFormat setParameters(Object... parameters);

    NutsStringFormat setParameters(List<Object> parameters);

    /**
     * return current value to format.
     *
     * @return current value to format
     * @since 0.5.6
     */
    String getString();

    NutsStringFormat of(String value, Object... parameters);

    NutsStringFormat append(String value, Object... parameters);

    /**
     * set current value to format.
     *
     * @param value value to format
     * @return {@code this} instance
     * @since 0.5.6
     */
    NutsStringFormat set(String value);

    /**
     * set current value to format.
     *
     * @param value value to format
     * @return {@code this} instance
     * @since 0.5.6
     */
    NutsStringFormat setString(Object value);

    /**
     * set current session.
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsStringFormat setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsStringFormat configure(boolean skipUnsupported, String... args);

}

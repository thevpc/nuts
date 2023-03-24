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
package net.thevpc.nuts.format;

import net.thevpc.nuts.NContentType;
import net.thevpc.nuts.NContentTypeFormat;
import net.thevpc.nuts.NExtensions;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;

import java.util.Map;

/**
 * Object format is responsible of formatting to terminal
 * a given object. Multiple implementation should be available
 * to support tables, trees, json, xml,...
 *
 * @author thevpc
 * @app.category Format
 */
public interface NObjectFormat extends NContentTypeFormat {

    static NObjectFormat of(NSession session) {
       return NExtensions.of(session).createSupported(NObjectFormat.class);
    }

    String getFormatMode();

    NObjectFormat setFormatMode(String formatMode);

    String getFormatString();

    NObjectFormat setFormatString(String formatString);

    Map<String, Object> getFormatParams();

    NObjectFormat setFormatParams(Map<String, Object> formatParams);

    NObjectFormat setFormatParam(String name, Object value);

    NContentType getOutputFormat();

    NObjectFormat setOutputFormat(NContentType outputFormat);

    /**
     * return value to format
     *
     * @return value to format
     */
    Object getValue();

    /**
     * set value to format
     *
     * @param value value to format
     * @return {@code this} instance
     */
    NObjectFormat setValue(Object value);

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NObjectFormat setSession(NSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NObjectFormat configure(boolean skipUnsupported, String... args);

    /**
     * true is compact json flag is armed
     *
     * @return true is compact json flag is armed
     */
    boolean isCompact();

    /**
     * enable compact json
     *
     * @param compact true to enable compact mode
     * @return {@code this} instance
     */
    NObjectFormat setCompact(boolean compact);


    NObjectFormat setNtf(boolean ntf);
}

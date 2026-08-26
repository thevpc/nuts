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
package net.thevpc.nuts.text;

import net.thevpc.nuts.ext.NExtensions;
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
public interface NObjectObjectWriter extends NContentTypeWriter {

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NObjectObjectWriter of() {
       return NExtensions.of(NObjectObjectWriter.class);
    }

    /**
     * Format mode.
     *
     * @return format mode result
     */
    String formatMode();

    /**
     * Format mode.
     *
     * @param formatMode format mode
     * @return format mode result
     */
    NObjectObjectWriter formatMode(String formatMode);

    /**
     * Format string.
     *
     * @return format string result
     */
    String formatString();

    /**
     * Format string.
     *
     * @param formatString format string
     * @return format string result
     */
    NObjectObjectWriter formatString(String formatString);

    /**
     * Format params.
     *
     * @return format params result
     */
    Map<String, Object> formatParams();

    /**
     * Format params.
     *
     * @param formatParams format params
     * @return format params result
     */
    NObjectObjectWriter formatParams(Map<String, Object> formatParams);

    /**
     * Format param.
     *
     * @param name name
     * @param value value
     * @return format param result
     */
    NObjectObjectWriter formatParam(String name, Object value);

    /**
     * Output format.
     *
     * @return output format result
     */
    NContentType outputFormat();

    /**
     * Output format.
     *
     * @param outputFormat output format
     * @return output format result
     */
    NObjectObjectWriter outputFormat(NContentType outputFormat);

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
    NObjectObjectWriter configure(boolean skipUnsupported, String... args);

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
    NObjectObjectWriter compact(boolean compact);


    /**
     * Ntf.
     *
     * @param ntf ntf
     * @return ntf result
     */
    NObjectObjectWriter ntf(boolean ntf);
}

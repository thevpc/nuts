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
package net.thevpc.nuts.format;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;

/**
 * Object format is responsible of formatting to terminal
 * a given object. Multiple implementation should be available
 * to support tables, trees, json, xml,...
 *
 * @author thevpc
 * @app.category Format
 * @since 0.8.3
 */
public interface NContentTypeFormat extends NFormat {

    static NContentTypeFormat of() {
       return NExtensions.of(NContentTypeFormat.class);
    }

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
    NContentTypeFormat setValue(Object value);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NContentTypeFormat configure(boolean skipUnsupported, String... args);

    NContentTypeFormat setNtf(boolean ntf);
}

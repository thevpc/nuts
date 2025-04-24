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
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

/**
 * @author thevpc
 * @app.category Command Line
 * @since 0.8.3
 */
public interface NCmdLines extends NComponent {
    static NCmdLines of() {
        return NExtensions.of(NCmdLines.class);
    }

    /**
     * return new Command line instance
     *
     * @param line command line to parse
     * @return new Command line instance
     */
    NOptional<NCmdLine> parseCmdLine(String line);

    /**
     * create argument name
     *
     * @param type create argument type
     * @return argument name
     */
    default NArgName createName(String type) {
        return createName(type, type);
    }

    /**
     * create argument name
     *
     * @param type  argument type
     * @param label argument label
     * @return argument name
     */
    NArgName createName(String type, String label);


    /**
     * return command line family
     *
     * @return command line family
     * @since 0.8.1
     */
    NShellFamily getShellFamily();

    /**
     * change command line family
     *
     * @param family family
     * @return {@code this} instance
     */
    NCmdLines setShellFamily(NShellFamily family);

    NCmdLines setLenient(boolean lenient);

    boolean isLenient();
}

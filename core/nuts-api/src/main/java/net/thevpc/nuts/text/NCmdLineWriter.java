/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.cmdline.NCmdLineFormatStrategy;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.platform.NShellFamily;

/**
 * Simple Command line Format
 *
 * @author thevpc
 * @app.category Command Line
 * @since 0.5.7
 */
public interface NCmdLineWriter extends NObjectWriter {

    /**
     * Creates a new instance of of plain.
     *
     * @return of plain result
     */
    static NCmdLineWriter ofPlain() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false ).ntf(false
         * @return of result
         */
        return of().ntf(false);
    }

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NCmdLineWriter of() {
       return NExtensions.of(NCmdLineWriter.class);
    }

    /**
     * return command line family
     *
     * @return command line family
     * @since 0.8.1
     */
    NShellFamily shellFamily();

    /**
     * change command line family
     *
     * @param family family
     * @return {@code this} instance
     */
    NCmdLineWriter shellFamily(NShellFamily family);


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
    NCmdLineWriter configure(boolean skipUnsupported, String... args);

    /**
     * Checks if is ntf.
     *
     * @return is ntf result
     */
    boolean isNtf();

    /**
     * Ntf.
     *
     * @param ntf ntf
     * @return ntf result
     */
    NCmdLineWriter ntf(boolean ntf);

    /**
     * Format strategy.
     *
     * @return format strategy result
     */
    NCmdLineFormatStrategy formatStrategy();

    /**
     * Format strategy.
     *
     * @param formatStrategy format strategy
     */
    void formatStrategy(NCmdLineFormatStrategy formatStrategy);
}

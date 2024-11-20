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
import net.thevpc.nuts.format.NFormat;
import net.thevpc.nuts.NShellFamily;

/**
 * Simple Command line Format
 *
 * @author thevpc
 * @app.category Command Line
 * @since 0.5.7
 */
public interface NCmdLineFormat extends NFormat {

    static NCmdLineFormat of() {
       return NExtensions.of().createComponent(NCmdLineFormat.class).get();
    }

    /**
     * return current command line
     *
     * @return current command line
     */
    NCmdLine getValue();

    /**
     * set command line
     *
     * @param value value
     * @return {@code this} instance
     */
    NCmdLineFormat setValue(NCmdLine value);

    /**
     * set command line from string array
     *
     * @param args args
     * @return {@code this} instance
     */
    NCmdLineFormat setValue(String[] args);

    /**
     * set command line from parsed string
     *
     * @param args args
     * @return {@code this} instance
     */
    NCmdLineFormat setValue(String args);

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
    NCmdLineFormat setShellFamily(NShellFamily family);


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
    NCmdLineFormat configure(boolean skipUnsupported, String... args);

    boolean isNtf();

    NCmdLineFormat setNtf(boolean ntf);

    NCmdLineFormatStrategy getFormatStrategy();

    void setFormatStrategy(NCmdLineFormatStrategy formatStrategy);
}

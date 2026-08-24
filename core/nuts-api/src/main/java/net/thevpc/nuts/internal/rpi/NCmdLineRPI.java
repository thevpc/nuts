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
package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

/**
 * @author thevpc
 * @app.category Command Line
 * @since 0.8.3
 */
public interface NCmdLineRPI extends NComponent {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NCmdLineRPI of() {
        return NExtensions.of(NCmdLineRPI.class);
    }

    /**
     * Parse cmd line.
     *
     * @param line line
     * @param family family
     * @param lenient lenient
     * @return parse cmd line result
     */
    NOptional<NCmdLine> parseCmdLine(String line, NShellFamily family, boolean lenient);

    /**
     * Creates a new instance of create cmd line by args.
     *
     * @param args args
     * @param family family
     * @return create cmd line by args result
     */
    NCmdLine createCmdLineByArgs(String[] args, NShellFamily family);


}

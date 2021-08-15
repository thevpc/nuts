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
 *
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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.*;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;

/**
 * Base Format Interface used to print "things".
 * @author thevpc
 * @since 0.8.1
 * @app.category SPI Base
 */
public interface NutsFormatSPI  {

    /**
     * format current value and write result to {@code out} and finally appends
     * a new line.
     *
     * @param out recipient print stream
     */
    void print(NutsPrintStream out);

    /**
     * ask {@code this} instance to configure with the very first argument of
     * {@code commandLine}. If the first argument is not supported, return
     * {@code false} and consume (skip/read) the argument. If the argument
     * required one or more parameters, these arguments are also consumed and
     * finally return {@code true}
     *
     * @param commandLine arguments to configure with
     * @return true when the at least one argument was processed
     */
    boolean configureFirst(NutsCommandLine commandLine);

}

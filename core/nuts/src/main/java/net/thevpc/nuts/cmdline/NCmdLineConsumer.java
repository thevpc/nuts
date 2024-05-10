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
package net.thevpc.nuts.cmdline;

/**
 * The consumer is called to process the command line arguments.
 *
 * @author thevpc
 * @app.category Command Line
 */
public interface NCmdLineConsumer {
    NCmdLineConsumer NOP = new NCmdLineConsumer() {
        @Override
        public boolean next(NArg arg, NCmdLine cmdLine, NCmdLineContext context) {
            cmdLine.next();
            return true;
        }
    };

    /**
     * process the given argument that was peeked from the command line.Implementations <strong>MUST</strong> call one of
     * the "next" methods to
     *
     * @param arg  peeked argument
     * @param cmdLine associated cmdLine
     * @param context session
     * @return true if the argument can be processed, false otherwise.
     */
    default boolean next(NArg arg, NCmdLine cmdLine, NCmdLineContext context) {
        return false;
    }

}

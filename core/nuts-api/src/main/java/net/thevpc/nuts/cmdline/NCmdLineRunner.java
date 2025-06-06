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

/**
 * The processor is called to process the command line arguments.
 * <ul>
 *  <li>{@code init}: called initially</li>
 *  <li>{@code processOption}|{@code processNonOption}: called multiple times until the command line is consumed</li>
 *  <li>{@code prepare}: called when the command line is fully consumed</li>
 *  <li>{@code exec}|{@code autoComplete}: called to process execution of autcomplete</li>
 * </ul>
 *
 * @author thevpc
 * @app.category Command Line
 */
public interface NCmdLineRunner {
    NCmdLineRunner NOP = new NCmdLineRunner() {
    };

    /**
     * process the given option argument that was peeked from the command line.Implementations <strong>MUST</strong> call one of
     * the "next" methods to
     *
     * @param option  peeked argument
     * @param cmdLine associated cmdLine
     * @return true if the argument can be processed, false otherwise.
     */
    default boolean nextOption(NArg option, NCmdLine cmdLine) {
        return false;
    }

    /**
     * process the given non option argument that was peeked from the command line.
     * Implementations <strong>MUST</strong> call one of
     * the "next" methods to
     *
     * @param nonOption peeked argument
     * @param cmdLine   associated cmdLine
     * @return true if the argument can be processed, false otherwise.
     */
    default boolean nextNonOption(NArg nonOption, NCmdLine cmdLine) {
        return false;
    }

    /**
     * initialize the processor.
     * Called before any other method.
     *
     * @param cmdLine associated cmdLine
     */
    default void init(NCmdLine cmdLine) {

    }

    /**
     * prepare for execution for auto-complete and/or exec modes.
     * Called after all next methods and before execute and autoComplete methods
     *
     * @param cmdLine associated cmdLine
     */
    default void validate(NCmdLine cmdLine) {

    }

    /**
     * execute options, called after all options was processed and
     * cmdLine.isExecMode() returns true.
     *
     * @param cmdLine associated cmdLine
     */
    default void run(NCmdLine cmdLine) {

    }

    /**
     * called when auto-complete ({@code autoComplete} is not null)
     *
     * @param cmdLine cmdLine
     */
    default void autoComplete(NCmdLine cmdLine) {

    }

}

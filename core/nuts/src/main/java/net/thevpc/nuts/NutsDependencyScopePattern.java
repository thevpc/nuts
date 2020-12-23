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
package net.thevpc.nuts;

/**
 * Supported dependency scope pattern.
 * A dependency scope pattern
 * @author thevpc
 * @since 0.5.6
 * @category Descriptor
 */
public enum NutsDependencyScopePattern {
    /**
     * api (gradle) / compile (maven)
     */
    API,

    /**
     * implementation (gradle) / compile (maven)
     */
    IMPLEMENTATION,

    /**
     * provided (gradle) / provided (maven)
     */
    PROVIDED,

    /**
     * import (maven)
     */
    IMPORT,

    /**
     * testRuntime (gradle) / runtime (maven)
     */
    RUNTIME,
    /**
     * system (maven)
     */
    SYSTEM,

    /**
     * testCompile (gradle) / test (maven)
     */
    TEST_COMPILE,

    /**
     * testCompileOnly (gradle)
     */
    TEST_PROVIDED,

    /**
     * testRuntime (gradle)
     */
    TEST_RUNTIME,

    /**
     * other
     */
    OTHER,

    /**
     * dependencies needed for running/executing unit tests the nuts : includes
     * 'test,compile,system,runtime' witch are NOT optional
     */
    TEST,

    /**
     * maven compile
     */
    COMPILE,
    /**
     * dependencies needed for running/executing the nuts : includes
     * 'compile,system,runtime' witch are NOT optional
     */
    RUN,

    /**
     * run test
     */
    RUN_TEST,
    /**
     * all dependencies (no restriction)
     */
    ALL;

    /**
     * lower-cased identifier for the enum entry
     */
    private String id;

    NutsDependencyScopePattern() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    /**
     * lower cased identifier.
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }



}

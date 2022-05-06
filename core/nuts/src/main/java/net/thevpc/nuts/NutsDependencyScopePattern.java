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

import net.thevpc.nuts.util.NutsNameFormat;
import net.thevpc.nuts.util.NutsStringUtils;

import java.util.EnumSet;

/**
 * Supported dependency scope pattern.
 * A dependency scope pattern
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.6
 */
public enum NutsDependencyScopePattern implements NutsEnum {
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
     * equivalent to maven's test
     */
    TEST_API,
    /**
     * equivalent to maven's test
     */
    TEST_IMPLEMENTATION,

    /**
     * testCompileOnly (gradle)
     */
    TEST_PROVIDED,

    /**
     * testRuntime (gradle)
     */
    TEST_RUNTIME,

    /**
     * dependencies needed for test execution
     */
    TEST_SYSTEM,

    /**
     * other
     */
    OTHER,
    /**
     * dependencies needed for test execution
     */
    TEST_OTHER,

    /**
     * [PATTERN] testCompile (gradle) / test (maven)
     */
    TEST_COMPILE,

    /**
     * [PATTERN] dependencies needed for running/executing unit tests the nuts : includes
     * 'test,compile,system,runtime' witch are NOT optional
     */
    TEST,

    /**
     * [PATTERN] maven compile
     */
    COMPILE,
    /**
     * [PATTERN] dependencies needed for running/executing the nuts : includes
     * 'compile,system,runtime' witch are NOT optional
     */
    RUN,

    /**
     * [PATTERN] run test
     */
    RUN_TEST,
    /**
     * [PATTERN] all dependencies (no restriction)
     */
    ALL,
    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsDependencyScopePattern() {
        this.id = NutsNameFormat.ID_NAME.formatName(name());
    }

    public static NutsOptional<NutsDependencyScopePattern> parse(String value) {
        return NutsStringUtils.parseEnum(value, NutsDependencyScopePattern.class, s->{
            switch (s.getNormalizedValue()) {
                case "COMPILEONLY": //gradle
                case "COMPILE_ONLY": //gradle
                case "PROVIDED": //gradle
                    return NutsOptional.of(NutsDependencyScopePattern.PROVIDED);
//            case "test"://maven
                case "TESTCOMPILE"://gradle
//            case "test_compile":
                case "TESTAPI":
                case "TEST_API":
                    return NutsOptional.of(NutsDependencyScopePattern.TEST_API);
                case "TESTRUNTIME":
                case "TEST_RUNTIME":
                    return NutsOptional.of(NutsDependencyScopePattern.TEST_RUNTIME);
                case "TESTSYSTEM":
                case "TEST_SYSTEM":
                    return NutsOptional.of(NutsDependencyScopePattern.TEST_SYSTEM);
                case "TESTPROVIDED":
                case "TEST_PROVIDED":
                case "TESTCOMPILEONLY":
                case "TEST_COMPILE_ONLY":
                    return NutsOptional.of(NutsDependencyScopePattern.TEST_PROVIDED);
                case "API":
//            case "compile":
                    return NutsOptional.of(NutsDependencyScopePattern.API);
                case "IMPL":
                case "IMPLEMENTATION":
                    return NutsOptional.of(NutsDependencyScopePattern.IMPLEMENTATION);
                case "IMPORT":
                    return NutsOptional.of(NutsDependencyScopePattern.IMPORT);
                case "RUNTIME":
                    return NutsOptional.of(NutsDependencyScopePattern.RUNTIME);
                case "TEST_IMPL":
                case "TEST_IMPLEMENTATION":
                    return NutsOptional.of(NutsDependencyScopePattern.TEST_IMPLEMENTATION);
                case "TEST_OTHER":
                    return NutsOptional.of(NutsDependencyScopePattern.TEST_OTHER);
                case "OTHER":
                    return NutsOptional.of(NutsDependencyScopePattern.OTHER);
                case "SYSTEM":
                    return NutsOptional.of(NutsDependencyScopePattern.SYSTEM);

                case "TEST_COMPILE":
                    return NutsOptional.of(NutsDependencyScopePattern.TEST_COMPILE);
                case "TEST":
                    return NutsOptional.of(NutsDependencyScopePattern.TEST);
                case "COMPILE":
                    return NutsOptional.of(NutsDependencyScopePattern.COMPILE);
                case "RUN":
                    return NutsOptional.of(NutsDependencyScopePattern.RUN);
                case "RUN_TEST":
                    return NutsOptional.of(NutsDependencyScopePattern.RUN_TEST);
                case "ALL":
                    return NutsOptional.of(NutsDependencyScopePattern.ALL);
            }
            return null;
        });
    }


    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }

    public EnumSet<NutsDependencyScope> toScopes() {
        EnumSet<NutsDependencyScope> v = EnumSet.noneOf(NutsDependencyScope.class);
        switch (this) {
            case RUN: {
                v.add(NutsDependencyScope.API);
                v.add(NutsDependencyScope.IMPLEMENTATION);
                v.add(NutsDependencyScope.SYSTEM);
                v.add(NutsDependencyScope.RUNTIME);
                break;
            }
            case RUN_TEST: {
                v.addAll(NutsDependencyScopePattern.RUN.toScopes());
                v.add(NutsDependencyScope.TEST_API);
                v.add(NutsDependencyScope.TEST_RUNTIME);
                break;
            }
            case COMPILE: {
                v.add(NutsDependencyScope.API);
                v.add(NutsDependencyScope.IMPLEMENTATION);
                v.add(NutsDependencyScope.SYSTEM);
                v.add(NutsDependencyScope.PROVIDED);
                break;
            }
            case TEST: {
                v.add(NutsDependencyScope.TEST_API);
                v.add(NutsDependencyScope.TEST_RUNTIME);
                v.add(NutsDependencyScope.TEST_PROVIDED);
                break;
            }
            case ALL: {
                v.add(NutsDependencyScope.API);
                v.add(NutsDependencyScope.IMPLEMENTATION);
                v.add(NutsDependencyScope.RUNTIME);
                v.add(NutsDependencyScope.SYSTEM);
                v.add(NutsDependencyScope.PROVIDED);
                v.add(NutsDependencyScope.TEST_API);
                v.add(NutsDependencyScope.TEST_RUNTIME);
                v.add(NutsDependencyScope.TEST_PROVIDED);
                v.add(NutsDependencyScope.OTHER);
                break;
            }
            case API: {
                v.add(NutsDependencyScope.API);
            }
            case IMPORT: {
                v.add(NutsDependencyScope.IMPORT);
            }
            case IMPLEMENTATION: {
                v.add(NutsDependencyScope.IMPLEMENTATION);
            }
            case PROVIDED: {
                v.add(NutsDependencyScope.PROVIDED);
            }
            case RUNTIME: {
                v.add(NutsDependencyScope.RUNTIME);
            }
            case SYSTEM: {
                v.add(NutsDependencyScope.SYSTEM);
            }
            case TEST_COMPILE: {
                v.add(NutsDependencyScope.TEST_API);
            }
            case TEST_PROVIDED: {
                v.add(NutsDependencyScope.TEST_PROVIDED);
            }
            case TEST_RUNTIME: {
                v.add(NutsDependencyScope.TEST_RUNTIME);
            }
            case TEST_SYSTEM: {
                v.add(NutsDependencyScope.TEST_SYSTEM);
            }
            case TEST_API: {
                v.add(NutsDependencyScope.TEST_API);
            }
            case TEST_IMPLEMENTATION: {
                v.add(NutsDependencyScope.TEST_IMPLEMENTATION);
            }
            case TEST_OTHER: {
                v.add(NutsDependencyScope.TEST_OTHER);
            }
            case OTHER: {
                v.add(NutsDependencyScope.OTHER);
            }
            default: {
                throw new IllegalArgumentException("unsupported scope pattern " + this);
            }
        }
        return v;
    }
}

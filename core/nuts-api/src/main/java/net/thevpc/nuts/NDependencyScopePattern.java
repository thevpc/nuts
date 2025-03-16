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
package net.thevpc.nuts;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

import java.util.EnumSet;

/**
 * Supported dependency scope pattern.
 * A dependency scope pattern
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.6
 */
public enum NDependencyScopePattern implements NEnum {
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

    NDependencyScopePattern() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NDependencyScopePattern> parse(String value) {
        return NEnumUtils.parseEnum(value, NDependencyScopePattern.class, s->{
            switch (s.getNormalizedValue()) {
                case "COMPILEONLY": //gradle
                case "COMPILE_ONLY": //gradle
                case "PROVIDED": //gradle
                    return NOptional.of(NDependencyScopePattern.PROVIDED);
//            case "test"://maven
                case "TESTCOMPILE"://gradle
//            case "test_compile":
                case "TESTAPI":
                case "TEST_API":
                    return NOptional.of(NDependencyScopePattern.TEST_API);
                case "TESTRUNTIME":
                case "TEST_RUNTIME":
                    return NOptional.of(NDependencyScopePattern.TEST_RUNTIME);
                case "TESTSYSTEM":
                case "TEST_SYSTEM":
                    return NOptional.of(NDependencyScopePattern.TEST_SYSTEM);
                case "TESTPROVIDED":
                case "TEST_PROVIDED":
                case "TESTCOMPILEONLY":
                case "TEST_COMPILE_ONLY":
                    return NOptional.of(NDependencyScopePattern.TEST_PROVIDED);
                case "API":
//            case "compile":
                    return NOptional.of(NDependencyScopePattern.API);
                case "IMPL":
                case "IMPLEMENTATION":
                    return NOptional.of(NDependencyScopePattern.IMPLEMENTATION);
                case "IMPORT":
                    return NOptional.of(NDependencyScopePattern.IMPORT);
                case "RUNTIME":
                    return NOptional.of(NDependencyScopePattern.RUNTIME);
                case "TEST_IMPL":
                case "TEST_IMPLEMENTATION":
                    return NOptional.of(NDependencyScopePattern.TEST_IMPLEMENTATION);
                case "TEST_OTHER":
                    return NOptional.of(NDependencyScopePattern.TEST_OTHER);
                case "OTHER":
                    return NOptional.of(NDependencyScopePattern.OTHER);
                case "SYSTEM":
                    return NOptional.of(NDependencyScopePattern.SYSTEM);

                case "TEST_COMPILE":
                    return NOptional.of(NDependencyScopePattern.TEST_COMPILE);
                case "TEST":
                    return NOptional.of(NDependencyScopePattern.TEST);
                case "COMPILE":
                    return NOptional.of(NDependencyScopePattern.COMPILE);
                case "RUN":
                    return NOptional.of(NDependencyScopePattern.RUN);
                case "RUN_TEST":
                    return NOptional.of(NDependencyScopePattern.RUN_TEST);
                case "ALL":
                    return NOptional.of(NDependencyScopePattern.ALL);
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

    public EnumSet<NDependencyScope> toScopes() {
        EnumSet<NDependencyScope> v = EnumSet.noneOf(NDependencyScope.class);
        switch (this) {
            case RUN: {
                v.add(NDependencyScope.API);
                v.add(NDependencyScope.IMPLEMENTATION);
                v.add(NDependencyScope.SYSTEM);
                v.add(NDependencyScope.RUNTIME);
                break;
            }
            case RUN_TEST: {
                v.addAll(NDependencyScopePattern.RUN.toScopes());
                v.add(NDependencyScope.TEST_API);
                v.add(NDependencyScope.TEST_RUNTIME);
                break;
            }
            case COMPILE: {
                v.add(NDependencyScope.API);
                v.add(NDependencyScope.IMPLEMENTATION);
                v.add(NDependencyScope.SYSTEM);
                v.add(NDependencyScope.PROVIDED);
                break;
            }
            case TEST: {
                v.add(NDependencyScope.TEST_API);
                v.add(NDependencyScope.TEST_RUNTIME);
                v.add(NDependencyScope.TEST_PROVIDED);
                break;
            }
            case ALL: {
                v.add(NDependencyScope.API);
                v.add(NDependencyScope.IMPLEMENTATION);
                v.add(NDependencyScope.RUNTIME);
                v.add(NDependencyScope.SYSTEM);
                v.add(NDependencyScope.PROVIDED);
                v.add(NDependencyScope.TEST_API);
                v.add(NDependencyScope.TEST_RUNTIME);
                v.add(NDependencyScope.TEST_PROVIDED);
                v.add(NDependencyScope.OTHER);
                break;
            }
            case API: {
                v.add(NDependencyScope.API);
            }
            case IMPORT: {
                v.add(NDependencyScope.IMPORT);
            }
            case IMPLEMENTATION: {
                v.add(NDependencyScope.IMPLEMENTATION);
            }
            case PROVIDED: {
                v.add(NDependencyScope.PROVIDED);
            }
            case RUNTIME: {
                v.add(NDependencyScope.RUNTIME);
            }
            case SYSTEM: {
                v.add(NDependencyScope.SYSTEM);
            }
            case TEST_COMPILE: {
                v.add(NDependencyScope.TEST_API);
            }
            case TEST_PROVIDED: {
                v.add(NDependencyScope.TEST_PROVIDED);
            }
            case TEST_RUNTIME: {
                v.add(NDependencyScope.TEST_RUNTIME);
            }
            case TEST_SYSTEM: {
                v.add(NDependencyScope.TEST_SYSTEM);
            }
            case TEST_API: {
                v.add(NDependencyScope.TEST_API);
            }
            case TEST_IMPLEMENTATION: {
                v.add(NDependencyScope.TEST_IMPLEMENTATION);
            }
            case TEST_OTHER: {
                v.add(NDependencyScope.TEST_OTHER);
            }
            case OTHER: {
                v.add(NDependencyScope.OTHER);
            }
            default: {
                throw new IllegalArgumentException("unsupported scope pattern " + this);
            }
        }
        return v;
    }
}

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
package net.thevpc.nuts;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * Supported dependency scope lists
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.4
 */
public enum NDependencyScope implements NEnum {
    /**
     * equivalent to maven's compile and to gradle's api
     */
    API,

    /**
     * equivalent to gradle's implementation
     */
    IMPLEMENTATION,

    /**
     * equivalent to maven's provided
     */
    PROVIDED,

    /**
     * equivalent to maven's import
     */
    IMPORT,

    /**
     * equivalent to maven's runtime
     */
    RUNTIME,
    /**
     * equivalent to maven's system
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
     * dependencies needed for test but are provided by container.
     */
    TEST_PROVIDED,

    /**
     * dependencies needed for test execution
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
    TEST_OTHER;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;
    private final boolean api;
    private final boolean implementation;
    private final boolean test;
    private final boolean system;
    private final boolean runtime;
    private final boolean provided;
    private final boolean other;


    /**
     * default constructor
     */
    NDependencyScope() {
        this.id = NNameFormat.ID_NAME.format(name());
        this.api = id.equals("api") || id.equals("test");
        this.implementation = id.equals("implementation") || id.endsWith("-implementation");
        this.provided = id.equals("provided") || id.endsWith("-provided");
        this.runtime = id.equals("provided") || id.endsWith("-runtime");
        this.system = id.equals("system") || id.endsWith("-system");
        this.test = id.startsWith("test-");
        this.other = id.equals("other") || id.startsWith("other-");
    }

    public static NOptional<NDependencyScope> parse(String value) {
        return NEnumUtils.parseEnum(value, NDependencyScope.class, s -> {
            switch (s.getNormalizedValue()) {
                case "COMPILEONLY": //gradle
                case "COMPILE_ONLY": //gradle
                case "PROVIDED": //gradle
                    return NOptional.of(NDependencyScope.PROVIDED);
                case "TEST"://maven
                case "TESTCOMPILE"://gradle
                case "TEST_COMPILE":
                case "TESTAPI":
                case "TEST_API":
                    return NOptional.of(NDependencyScope.TEST_API);
                case "TESTRUNTIME":
                case "TEST_RUNTIME":
                    return NOptional.of(NDependencyScope.TEST_RUNTIME);
                case "TESTSYSTEM":
                case "TEST_SYSTEM":
                    return NOptional.of(NDependencyScope.TEST_SYSTEM);
                case "TESTPROVIDED":
                case "TEST_PROVIDED":
                case "TESTCOMPILEONLY":
                case "TEST_COMPILE_ONLY":
                    return NOptional.of(NDependencyScope.TEST_PROVIDED);
                case "API":
                case "COMPILE":
                    return NOptional.of(NDependencyScope.API);
                case "IMPL":
                case "IMPLEMENTATION":
                    return NOptional.of(NDependencyScope.IMPLEMENTATION);
                case "IMPORT":
                    return NOptional.of(NDependencyScope.IMPORT);
                case "RUNTIME":
                    return NOptional.of(NDependencyScope.RUNTIME);
                case "TEST_IMPL":
                case "TEST_IMPLEMENTATION":
                    return NOptional.of(NDependencyScope.TEST_IMPLEMENTATION);
                case "TEST_OTHER":
                    return NOptional.of(NDependencyScope.TEST_OTHER);
                case "OTHER":
                    return NOptional.of(NDependencyScope.OTHER);
                case "SYSTEM":
                    return NOptional.of(NDependencyScope.SYSTEM);
            }
            return null;
        });
    }

    public boolean isCompile() {
        return !test;
    }

    public boolean isApi() {
        return api;
    }

    public boolean isImplementation() {
        return implementation;
    }

    public boolean isTest() {
        return test;
    }

    public boolean isSystem() {
        return system;
    }

    public boolean isRuntime() {
        return runtime;
    }

    public boolean isProvided() {
        return provided;
    }

    public boolean isOther() {
        return other;
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}

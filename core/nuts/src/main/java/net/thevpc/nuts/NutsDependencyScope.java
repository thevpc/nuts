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

import net.thevpc.nuts.reserved.NutsReservedLangUtils;

/**
 * Supported dependency scope lists
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.4
 */
public enum NutsDependencyScope implements NutsEnum {
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
    NutsDependencyScope() {
        this.id = name().toLowerCase().replace('_', '-');
        api = id.equals("api") || id.equals("test");
        implementation = id.equals("implementation") || id.endsWith("-implementation");
        provided = id.equals("provided") || id.endsWith("-provided");
        runtime = id.equals("provided") || id.endsWith("-runtime");
        system = id.equals("system") || id.endsWith("-system");
        test = id.startsWith("test-");
        other = id.equals("other") || id.startsWith("other-");
    }

    public static NutsOptional<NutsDependencyScope> parse(String value) {
        return NutsReservedLangUtils.parseEnum(value, NutsDependencyScope.class, s->{
            switch (s.toLowerCase()) {
                case "compileonly": //gradle
                case "compile_only": //gradle
                case "provided": //gradle
                    return NutsOptional.of(NutsDependencyScope.PROVIDED);
                case "test"://maven
                case "testcompile"://gradle
                case "test_compile":
                case "testapi":
                case "test_api":
                    return NutsOptional.of(NutsDependencyScope.TEST_API);
                case "testruntime":
                case "test_runtime":
                    return NutsOptional.of(NutsDependencyScope.TEST_RUNTIME);
                case "testsystem":
                case "test_system":
                    return NutsOptional.of(NutsDependencyScope.TEST_SYSTEM);
                case "testprovided":
                case "test_provided":
                case "testcompileonly":
                case "test_compile_only":
                    return NutsOptional.of(NutsDependencyScope.TEST_PROVIDED);
                case "api":
                case "compile":
                    return NutsOptional.of(NutsDependencyScope.API);
                case "impl":
                case "implementation":
                    return NutsOptional.of(NutsDependencyScope.IMPLEMENTATION);
                case "import":
                    return NutsOptional.of(NutsDependencyScope.IMPORT);
                case "runtime":
                    return NutsOptional.of(NutsDependencyScope.RUNTIME);
                case "test_impl":
                case "test_implementation":
                    return NutsOptional.of(NutsDependencyScope.TEST_IMPLEMENTATION);
                case "test_other":
                    return NutsOptional.of(NutsDependencyScope.TEST_OTHER);
                case "other":
                    return NutsOptional.of(NutsDependencyScope.OTHER);
                case "system":
                    return NutsOptional.of(NutsDependencyScope.SYSTEM);
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

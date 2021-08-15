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
package net.thevpc.nuts;

/**
 * Dependency Format Helper
 * @author thevpc
 * @since 0.5.6
 * @app.category Format
 */
public interface NutsDependencyFormat extends NutsFormat {

    /**
     * true if omit repository from formatted string.
     * @return true if omit repository from formatted string
     */
    boolean isOmitRepository();

    /**
     * if true omit (do not include) repository when formatting the value
     * set using {@link #setValue(NutsDependency)} .
     * @param omitRepository new value
     * @return {@code this} instance
     */
    NutsDependencyFormat setOmitRepository(boolean omitRepository);

    /**
     * return true if omit group
     * @return true if omit group
     */
    boolean isOmitGroupId();

    /**
     * if true omit (do not include) group when formatting the value
     * set using {@link #setValue(NutsDependency)} .
     * @param omitGroup new value
     * @return {@code this} instance
     */
    NutsDependencyFormat setOmitGroupId(boolean omitGroup);

    /**
     * omit imported group
     * @return omit imported group
     */
    boolean isOmitImportedGroupId();

    /**
     * if true omit (do not include) group (if the group is imported) when formatting the value
     * set using {@link #setValue(NutsDependency)} .
     * @param omitEnv new value
     * @return {@code this} instance
     */
    NutsDependencyFormat setOmitImportedGroup(boolean omitEnv);

    /**
     * return true if omit all query properties
     * @return true if omit all query properties
     */
    boolean isOmitOtherProperties();

    /**
     * if true omit (do not include) query (scope and optional) when formatting the value
     * set using {@link #setValue(NutsDependency)} .
     * @param value new value
     * @return {@code this} instance
     */
    NutsDependencyFormat setOmitOtherProperties(boolean value);

    /**
     * return true if omit scope
     * @return true if omit scope
     */
    boolean isOmitScope();

    /**
     * if true omit (do not include) face when formatting the value
     * set using {@link #setValue(NutsDependency)} .
     * @param value new value
     * @return {@code this} instance
     */
    NutsDependencyFormat setOmitScope(boolean value);

    /**
     * return true if omit scope
     * @return true if omit scope
     */
    boolean isOmitClassifier();

    /**
     * if true omit (do not include) face when formatting the value
     * set using {@link #setValue(NutsDependency)} .
     * @param value new value
     * @return {@code this} instance
     */
    NutsDependencyFormat setOmitClassifier(boolean value);

//    /**
//     * return true if omit alternative
//     * @return true if omit alternative
//     */
//    boolean isOmitAlternative();

//    /**
//     * if true omit (do not include) face when formatting the value
//     * set using {@link #setValue(NutsDependency)} .
//     * @param value new value
//     * @return {@code this} instance
//     */
//    NutsDependencyFormat setOmitAlternative(boolean value);
//
//    /**
//     * if true omit (do not include) face when formatting the value
//     * set using {@link #setValue(NutsDependency)} .
//     * @param value new value
//     * @return {@code this} instance
//     */
//    NutsDependencyFormat omitAlternative(boolean value);
//
//    /**
//     * omit alternative
//     * @return {@code this} instance
//     */
//    NutsDependencyFormat omitAlternative();

    /**
     * return true if omit exclusions
     * @return true if omit exclusions
     */
    boolean isOmitExclusions();

    /**
     * if true omit (do not include) face when formatting the value
     * set using {@link #setValue(NutsDependency)} .
     * @param value new value
     * @return {@code this} instance
     */
    NutsDependencyFormat setOmitExclusions(boolean value);

    /**
     * return true if omit optional
     * @return true if omit optional
     */
    boolean isOmitOptional();

    /**
     * if true omit (do not include) face when formatting the value
     * set using {@link #setValue(NutsDependency)} .
     * @param value new value
     * @return {@code this} instance
     */
    NutsDependencyFormat setOmitOptional(boolean value);

    /**
     * list of all omitted query properties
     * @return list of all omitted query properties
     */
    String[] getOmitQueryProperties();

    /**
     * return true if omit query property named {@code name}
     * @param name property name
     * @return true if omit query property named {@code name}
     */
    boolean isOmitQueryProperty(String name);

    /**
     * if true omit (do not include) query property named {@code name} when formatting the value
     * set using {@link #setValue(NutsDependency)} .
     * @param name property name
     * @param value new value
     * @return {@code this} instance
     */
    NutsDependencyFormat setOmitQueryProperty(String name,boolean value);


    /**
     * true if highlight (distinct color) imported group
     * @return true if highlight (distinct color) imported group
     */
    boolean isHighlightImportedGroup();

    /**
     * if true omit (do not include) name space when formatting the value
     * set using {@link #setValue(NutsDependency)} .
     * @param highlightImportedGroup new value
     * @return {@code this} instance
     */
    NutsDependencyFormat setHighlightImportedGroup(boolean highlightImportedGroup);

    /**
     * return true if scope is highlighted
     * @return true if scope is highlighted
     */
    boolean isHighlightScope();

    /**
     * if true omit (do not include) name space when formatting the value
     * set using {@link #setValue(NutsDependency)} .
     * @param highlightScope new value
     * @return {@code this} instance
     */
    NutsDependencyFormat setHighlightScope(boolean highlightScope);

    /**
     * return true if optional is highlighted
     * @return true if optional is highlighted
     */
    boolean isHighlightOptional();

    /**
     * if true omit (do not include) name space when formatting the value
     * set using {@link #setValue(NutsDependency)} .
     * @param highlightOptional new value
     * @return {@code this} instance
     */
    NutsDependencyFormat setHighlightOptional(boolean highlightOptional);

    /**
     * return current value to format
     * @return current value to format
     * @since 0.5.6
     */
    NutsDependency getValue();

    /**
     * value dependency to format
     * @param dependency dependency to format
     * @return {@code this} instance
     * @since 0.5.6
     */
    NutsDependencyFormat setValue(NutsDependency dependency);

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NutsDependencyFormat setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsDependencyFormat configure(boolean skipUnsupported, String... args);


    NutsDependencyFormat setNtf(boolean ntf);
}

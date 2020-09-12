/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 * Dependency Format Helper
 * @author vpc
 * @since 0.5.6
 * @category Format
 */
public interface NutsDependencyFormat extends NutsFormat {

    /**
     * true if omit namespace from formatted string.
     * @return true if omit namespace from formatted string
     */
    boolean isOmitNamespace();

    /**
     * if true omit (do not include) namespace when formatting the value
     * set using {@link #setValue(NutsDependency)} .
     * @param omitNamespace new value
     * @return {@code this} instance
     */
    NutsDependencyFormat setOmitNamespace(boolean omitNamespace);

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
     * return mutable id builder instance initialized with {@code this} instance.
     * @return mutable id builder instance initialized with {@code this} instance
     */
    NutsDependencyBuilder builder();

    /**
     * parse dependency in the form
     * namespace://group:name#version?scope=&lt;scope&gt;{@code &}optional=&lt;optional&gt;
     * If the string cannot be evaluated, return null.
     * @param dependency dependency
     * @return new instance of parsed dependency
     */
    NutsDependency parse(String dependency);

    /**
     * parse dependency in the form
     * namespace://group:name#version?scope=&lt;scope&gt;{@code &}optional=&lt;optional&gt;
     * If the string cannot be evaluated, return null.
     * @param dependency dependency
     * @return new instance of parsed dependency
     * @throws NutsParseException if the string cannot be evaluated
     */
    NutsDependency parseRequired(String dependency);

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
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsDependencyFormat configure(boolean skipUnsupported, String... args);

}

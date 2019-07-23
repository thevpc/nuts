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
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsIdFormat extends NutsFormat {

    boolean isOmitNamespace();

    NutsIdFormat setOmitNamespace(boolean omitNamespace);

    boolean isOmitGroup();

    NutsIdFormat setOmitGroup(boolean omitGroup);

    boolean isOmitImportedGroup();

    NutsIdFormat setOmitImportedGroup(boolean omitImportedGroup);

    boolean isOmitQuery();

    NutsIdFormat setOmitQuery(boolean omitEnv);

    boolean isOmitFace();

    NutsIdFormat setOmitFace(boolean omitFace);

    boolean isHighlightImportedGroup();

    NutsIdFormat setHighlightImportedGroup(boolean highlightImportedGroup);

    boolean isHighlightScope();

    NutsIdFormat setHighlightScope(boolean highlightScope);

    boolean isHighlightOptional();

    NutsIdFormat setHighlightOptional(boolean highlightOptional);

    /**
     * return true if omit scope
     * @return true if omit scope
     */
    boolean isOmitClassifier();

    /**
     * if true omit (do not include) face when formatting the value
     * set using {@link #setValue(NutsId)} .
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdFormat setOmitClassifier(boolean value);

    /**
     * if true omit (do not include) face when formatting the value
     * set using {@link #setValue(NutsId)} .
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdFormat omitClassifier(boolean value);

    /**
     * omit scope
     * @return {@code this} instance
     */
    NutsIdFormat omitClassifier();

    /**
     * return true if omit scope
     * @return true if omit scope
     */
    boolean isOmitAlternative();

    /**
     * if true omit (do not include) face when formatting the value
     * set using {@link #setValue(NutsId)} .
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdFormat setOmitAlternative(boolean value);

    /**
     * if true omit (do not include) face when formatting the value
     * set using {@link #setValue(NutsId)} .
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdFormat omitAlternative(boolean value);

    /**
     * omit scope
     * @return {@code this} instance
     */
    NutsIdFormat omitAlternative();

    /**
     * query properties omitted
     * @return query properties omitted
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
     * set using {@link #setValue(NutsId)} .
     * @param name property name
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdFormat setOmitQueryProperty(String name,boolean value);

    /**
     * if true omit (do not include) query property named {@code name} when formatting the value
     * set using {@link #setValue(NutsId)} .
     * @param name property name
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdFormat omitQueryProperty(String name,boolean value);

    /**
     * omit query property named {@code name}
     * @param name property name
     * @return {@code this} instance
     */
    NutsIdFormat omitQueryProperty(String name);

    /**
     * id to format
     * @return id to format
     * @since 0.5.6
     */
    NutsId getValue();


    /**
     * create new instance of id builder
     * @return new instance of id builder
     */
    NutsIdBuilder builder();

    /**
     * detect nuts id from resources containing the given class
     * or null if not found. If multiple resolutions return the first.
     * @param clazz to search for
     * @return nuts id detected from resources containing the given class
     */
    NutsId resolveId(Class clazz);

    /**
     * detect all nuts ids from resources containing the given class.
     * @param clazz to search for
     * @return all nuts ids detected from resources containing the given class
     */
    NutsId[] resolveIds(Class clazz);

    /**
     * set id to format
     * @param id id to format
     * @return {@code this} instance
     * @since 0.5.6
     */
    NutsIdFormat value(NutsId id);

    /**
     * id to format
     * @param id id to format
     * @return {@code this} instance
     * @since 0.5.6
     */
    NutsIdFormat setValue(NutsId id);

    /**
     * parse id or error if not valid
     * @param id to parse
     * @return parsed id
     */
    NutsId parseRequired(String id);

    /**
     * parse id or null if not valid.
     * id is parsed in the form
     * namespace://group:name#version?key=&lt;value&gt;{@code &}key=&lt;value&gt; ...
     * @param id to parse
     * @return parsed id
     * @throws NutsParseException if the string cannot be evaluated
     */
    NutsId parse(String id);

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NutsIdFormat session(NutsSession session);

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NutsIdFormat setSession(NutsSession session);

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
    NutsIdFormat configure(boolean skipUnsupported, String... args);

}

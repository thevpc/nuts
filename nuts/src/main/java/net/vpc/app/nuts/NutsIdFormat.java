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
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 * Class responsible of manipulating  {@link NutsId} instances:
 * <ul>
 *     <li>formatting (in Nuts Stream Format)</li>
 *     <li>parsing</li>
 * </ul>
 * @author vpc
 * @since 0.5.4
 * @category Format
 */
public interface NutsIdFormat extends NutsFormat {

    /**
     * return true when the namespace should not be included in formatted instance
     * @return return true when the namespace should not be included in formatted instance
     */
    boolean isOmitNamespace();

    /**
     * update omitNamespace
     * @param value true when the namespace should not be included in formatted instance
     * @return {@code this} instance
     */
    NutsIdFormat setOmitNamespace(boolean value);

    /**
     * update omitNamespace
     * @param value true when the namespace should not be included in formatted instance
     * @return {@code this} instance
     */
    NutsIdFormat omitNamespace(boolean value);

    /**
     * update omitNamespace to true
     * @return {@code this} instance
     */
    NutsIdFormat omitNamespace();

    /**
     * return true when the groupId should not be included in formatted instance
     * @return return true when the groupId should not be included in formatted instance
     */
    boolean isOmitGroupId();

    /**
     * update omitGroup
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdFormat setOmitGroupId(boolean value);

    /**
     * update omitGroup
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdFormat omitGroupId(boolean value);

    /**
     * update omitGroup to true
     * @return {@code this} instance
     */
    NutsIdFormat omitGroupId();

    /**
     * return true when the imported groupId should not be included in formatted instance
     * @return return true when the imported groupId should not be included in formatted instance
     */
    boolean isOmitImportedGroupId();

    /**
     * update omitImportedGroupId
     * @param value value
     * @return {@code this} instance
     */
    NutsIdFormat setOmitImportedGroupId(boolean value);

    /**
     * update omitImportedGroupId
     * @param value value
     * @return {@code this} instance
     */
    NutsIdFormat omitImportedGroupId(boolean value);

    /**
     * update omitImportedGroupId to ture
     * @return {@code this} instance
     */
    NutsIdFormat omitImportedGroupId();

    /**
     * return true if omit other properties
     * @return true if omit other properties
     */
    boolean isOmitOtherProperties();

    /**
     * update omitOtherProperties
     * @param value value
     * @return {@code this} instance
     */
    NutsIdFormat setOmitOtherProperties(boolean value);

    /**
     * update omitOtherProperties
     * @param value value
     * @return {@code this} instance
     */
    NutsIdFormat omitOtherProperties(boolean value);

    /**
     * update omitOtherProperties to true
     * @return {@code this} instance
     */
    NutsIdFormat omitOtherProperties();

    /**
     * return true when the face should not be included in formatted instance
     * @return return true when the face should not be included in formatted instance
     */
    boolean isOmitFace();

    /**
     * update omitFace
     * @param value value
     * @return {@code this} instance
     */
    NutsIdFormat setOmitFace(boolean value);

    /**
     * update omitFace
     * @param value value
     * @return {@code this} instance
     */
    NutsIdFormat omitFace(boolean value);

    /**
     * update omitFace to true
     * @return {@code this} instance
     */
    NutsIdFormat omitFace();

    /**
     * return true when the imported group should be highlighted in formatted instance
     * @return return true when the imported group should be highlighted in formatted instance
     */
    boolean isHighlightImportedGroupId();

    /**
     * update highlightImportedGroupId
     * @param value value
     * @return {@code this} instance
     */
    NutsIdFormat setHighlightImportedGroupId(boolean value);

    /**
     * update highlightImportedGroupId
     * @param value value
     * @return {@code this} instance
     */
    NutsIdFormat highlightImportedGroupId(boolean value);

    /**
     * update highlightImportedGroupId to true
     * @return {@code this} instance
     */
    NutsIdFormat highlightImportedGroupId();

    /**
     * return true when the scope should be highlighted in formatted instance
     * @return return true when the scope should be highlighted in formatted instance
     */
    boolean isHighlightScope();

    /**
     * update highlightScope
     * @param value value
     * @return {@code this} instance
     */
    NutsIdFormat setHighlightScope(boolean value);

    /**
     * update highlightScope
     * @param value value
     * @return {@code this} instance
     */
    NutsIdFormat highlightScope(boolean value);

    /**
     * update highlightScope to true
     * @return {@code this} instance
     */
    NutsIdFormat highlightScope();

    /**
     * return true when the optional should be highlighted in formatted instance
     * @return return true when the optional should be highlighted in formatted instance
     */
    boolean isHighlightOptional();

    /**
     * update highlightOptional
     * @param value value
     * @return {@code this} instance
     */
    NutsIdFormat setHighlightOptional(boolean value);

    /**
     * update highlightOptional
     * @param value value
     * @return {@code this} instance
     */
    NutsIdFormat highlightOptional(boolean value);

    /**
     * update highlightOptional tot true
     * @return {@code this} instance
     */
    NutsIdFormat highlightOptional();

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
     * query properties omitted
     * @return query properties omitted
     */
    String[] getOmitProperties();

    /**
     * return true if omit query property named {@code name}
     * @param name property name
     * @return true if omit query property named {@code name}
     */
    boolean isOmitProperty(String name);

    /**
     * if true omit (do not include) query property named {@code name} when formatting the value
     * set using {@link #setValue(NutsId)} .
     * @param name property name
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdFormat setOmitProperty(String name, boolean value);

    /**
     * if true omit (do not include) query property named {@code name} when formatting the value
     * set using {@link #setValue(NutsId)} .
     * @param name property name
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdFormat omitProperty(String name, boolean value);

    /**
     * omit query property named {@code name}
     * @param name property name
     * @return {@code this} instance
     */
    NutsIdFormat omitProperty(String name);

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
     * id to format
     * @param id id to format
     * @return {@code this} instance
     * @since 0.5.6
     */
    NutsIdFormat set(NutsId id);

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

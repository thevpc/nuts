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

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;

/**
 * Class responsible of manipulating  {@link NutsId} instances:
 * <ul>
 *     <li>formatting (in Nuts Stream Format)</li>
 *     <li>parsing</li>
 * </ul>
 * @author thevpc
 * @since 0.5.4
 * @app.category Format
 */
public interface NutsIdFormat extends NutsFormat, NutsComponent<Object> {
    static NutsIdFormat of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsIdFormat.class,true,null);
    }

    /**
     * return true when the repository should not be included in formatted instance
     * @return return true when the repository should not be included in formatted instance
     */
    boolean isOmitRepository();

    /**
     * update omitRepository
     * @param value true when the repository should not be included in formatted instance
     * @return {@code this} instance
     */
    NutsIdFormat setOmitRepository(boolean value);

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
     * id to format
     * @return id to format
     * @since 0.5.6
     */
    NutsId getValue();

    /**
     * id to format
     * @param id id to format
     * @return {@code this} instance
     * @since 0.5.6
     */
    NutsIdFormat setValue(NutsId id);

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
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsIdFormat configure(boolean skipUnsupported, String... args);


    NutsIdFormat setNtf(boolean ntf);

}

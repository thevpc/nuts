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

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.format.NFormat;
import net.thevpc.nuts.spi.NComponent;

import java.util.List;

/**
 * Class responsible of manipulating  {@link NId} instances:
 * <ul>
 *     <li>formatting (in Nuts Stream Format)</li>
 *     <li>parsing</li>
 * </ul>
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.4
 */
public interface NIdFormat extends NFormat, NComponent {
    static NIdFormat of(NId id) {
        return of().setValue(id);
    }

    static NIdFormat of() {
       return NExtensions.of(NIdFormat.class);
    }

    /**
     * return true when the repository should not be included in formatted instance
     *
     * @return return true when the repository should not be included in formatted instance
     */
    boolean isOmitRepository();

    /**
     * update omitRepository
     *
     * @param value true when the repository should not be included in formatted instance
     * @return {@code this} instance
     */
    NIdFormat setOmitRepository(boolean value);

    /**
     * return true when the groupId should not be included in formatted instance
     *
     * @return return true when the groupId should not be included in formatted instance
     */
    boolean isOmitGroupId();

    /**
     * update omitGroup
     *
     * @param value new value
     * @return {@code this} instance
     */
    NIdFormat setOmitGroupId(boolean value);

    /**
     * return true when the imported groupId should not be included in formatted instance
     *
     * @return return true when the imported groupId should not be included in formatted instance
     */
    boolean isOmitImportedGroupId();

    /**
     * update omitImportedGroupId
     *
     * @param value value
     * @return {@code this} instance
     */
    NIdFormat setOmitImportedGroupId(boolean value);

    /**
     * return true if omit other properties
     *
     * @return true if omit other properties
     */
    boolean isOmitOtherProperties();

    /**
     * update omitOtherProperties
     *
     * @param value value
     * @return {@code this} instance
     */
    NIdFormat setOmitOtherProperties(boolean value);

    /**
     * return true when the face should not be included in formatted instance
     *
     * @return return true when the face should not be included in formatted instance
     */
    boolean isOmitFace();

    /**
     * update omitFace
     *
     * @param value value
     * @return {@code this} instance
     */
    NIdFormat setOmitFace(boolean value);


    /**
     * return true when the imported group should be highlighted in formatted instance
     *
     * @return return true when the imported group should be highlighted in formatted instance
     */
    boolean isHighlightImportedGroupId();

    /**
     * update highlightImportedGroupId
     *
     * @param value value
     * @return {@code this} instance
     */
    NIdFormat setHighlightImportedGroupId(boolean value);

    /**
     * query properties omitted
     *
     * @return query properties omitted
     */
    List<String> getOmitProperties();

    /**
     * return true if omit query property named {@code name}
     *
     * @param name property name
     * @return true if omit query property named {@code name}
     */
    boolean isOmitProperty(String name);

    /**
     * if true omit (do not include) query property named {@code name} when formatting the value
     * set using {@link #setValue(NId)} .
     *
     * @param name  property name
     * @param value new value
     * @return {@code this} instance
     */
    NIdFormat setOmitProperty(String name, boolean value);

    /**
     * id to format
     *
     * @return id to format
     * @since 0.5.6
     */
    NId getValue();

    /**
     * id to format
     *
     * @param id id to format
     * @return {@code this} instance
     * @since 0.5.6
     */
    NIdFormat setValue(NId id);


    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NIdFormat configure(boolean skipUnsupported, String... args);


    NIdFormat setNtf(boolean ntf);

}

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
 *
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
package net.thevpc.nuts.format;

import net.thevpc.nuts.NDependency;
import net.thevpc.nuts.NDescriptor;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.util.List;

/**
 * Dependency Format Helper
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.6
 */
public interface NDependencyFormat extends NFormat, NComponent {

    static NDependencyFormat of(NDependency value) {
        return of().setValue(value);
    }

    static NDependencyFormat of() {
        return NExtensions.of(NDependencyFormat.class);
    }

    /**
     * true if omit repository from formatted string.
     *
     * @return true if omit repository from formatted string
     */
    boolean isOmitRepository();

    /**
     * if true omit (do not include) repository when formatting the value
     * set using {@link #setValue(NDependency)} .
     *
     * @param omitRepository new value
     * @return {@code this} instance
     */
    NDependencyFormat setOmitRepository(boolean omitRepository);

    /**
     * return true if omit group
     *
     * @return true if omit group
     */
    boolean isOmitGroupId();

    /**
     * if true omit (do not include) group when formatting the value
     * set using {@link #setValue(NDependency)} .
     *
     * @param omitGroup new value
     * @return {@code this} instance
     */
    NDependencyFormat setOmitGroupId(boolean omitGroup);

    /**
     * omit imported group
     *
     * @return omit imported group
     */
    boolean isOmitImportedGroupId();

    /**
     * if true omit (do not include) group (if the group is imported) when formatting the value
     * set using {@link #setValue(NDependency)} .
     *
     * @param omitEnv new value
     * @return {@code this} instance
     */
    NDependencyFormat setOmitImportedGroup(boolean omitEnv);

    /**
     * return true if omit all query properties
     *
     * @return true if omit all query properties
     */
    boolean isOmitOtherProperties();

    /**
     * if true omit (do not include) query (scope and optional) when formatting the value
     * set using {@link #setValue(NDependency)} .
     *
     * @param value new value
     * @return {@code this} instance
     */
    NDependencyFormat setOmitOtherProperties(boolean value);

    /**
     * return true if omit scope
     *
     * @return true if omit scope
     */
    boolean isOmitScope();

    /**
     * if true omit (do not include) face when formatting the value
     * set using {@link #setValue(NDependency)} .
     *
     * @param value new value
     * @return {@code this} instance
     */
    NDependencyFormat setOmitScope(boolean value);



    /**
     * return true if omit exclusions
     *
     * @return true if omit exclusions
     */
    boolean isOmitExclusions();

    /**
     * if true omit (do not include) face when formatting the value
     * set using {@link #setValue(NDependency)} .
     *
     * @param value new value
     * @return {@code this} instance
     */
    NDependencyFormat setOmitExclusions(boolean value);

    /**
     * return true if omit optional
     *
     * @return true if omit optional
     */
    boolean isOmitOptional();

    /**
     * if true omit (do not include) face when formatting the value
     * set using {@link #setValue(NDependency)} .
     *
     * @param value new value
     * @return {@code this} instance
     */
    NDependencyFormat setOmitOptional(boolean value);

    /**
     * list of all omitted query properties
     *
     * @return list of all omitted query properties
     */
    List<String> getOmitQueryProperties();

    /**
     * return true if omit query property named {@code name}
     *
     * @param name property name
     * @return true if omit query property named {@code name}
     */
    boolean isOmitQueryProperty(String name);

    /**
     * if true omit (do not include) query property named {@code name} when formatting the value
     * set using {@link #setValue(NDependency)} .
     *
     * @param name  property name
     * @param value new value
     * @return {@code this} instance
     */
    NDependencyFormat setOmitQueryProperty(String name, boolean value);


    /**
     * true if highlight (distinct color) imported group
     *
     * @return true if highlight (distinct color) imported group
     */
    boolean isHighlightImportedGroup();

    /**
     * if true omit (do not include) name space when formatting the value
     * set using {@link #setValue(NDependency)} .
     *
     * @param highlightImportedGroup new value
     * @return {@code this} instance
     */
    NDependencyFormat setHighlightImportedGroup(boolean highlightImportedGroup);

    /**
     * return current value to format
     *
     * @return current value to format
     * @since 0.5.6
     */
    NDependency getValue();

    /**
     * value dependency to format
     *
     * @param dependency dependency to format
     * @return {@code this} instance
     * @since 0.5.6
     */
    NDependencyFormat setValue(NDependency dependency);

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
    NDependencyFormat configure(boolean skipUnsupported, String... args);


    NDependencyFormat setNtf(boolean ntf);
}

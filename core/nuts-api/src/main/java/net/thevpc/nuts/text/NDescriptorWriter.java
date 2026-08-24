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
package net.thevpc.nuts.text;

import net.thevpc.nuts.artifact.NDescriptorStyle;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

/**
 * Descriptor Format class that help building, formatting and parsing Descriptors.
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.4
 */
public interface NDescriptorWriter extends NObjectWriter, NComponent {
    /**
     * Creates a new instance of of ntf.
     *
     * @return of ntf result
     */
    static NDescriptorWriter ofNtf() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(true ).ntf(true
         * @return of result
         */
        return of().ntf(true);
    }

    /**
     * Creates a new instance of of plain.
     *
     * @return of plain result
     */
    static NDescriptorWriter ofPlain() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false ).ntf(false
         * @return of result
         */
        return of().ntf(false);
    }

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NDescriptorWriter of() {
        return NExtensions.of(NDescriptorWriter.class);
    }

    /**
     * Creates a new instance of of ntf maven.
     *
     * @return of ntf maven result
     */
    static NDescriptorWriter ofNtfMaven() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(true).descriptorStyle(NDescriptorStyle.MAVEN ).ntf(true).descriptor style(n descriptor style.maven
         * @return of result
         */
        return of().ntf(true).descriptorStyle(NDescriptorStyle.MAVEN);
    }

    /**
     * Creates a new instance of of maven.
     *
     * @return of maven result
     */
    static NDescriptorWriter ofMaven() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).descriptorStyle(NDescriptorStyle.MAVEN ).ntf(false).descriptor style(n descriptor style.maven
         * @return of result
         */
        return of().ntf(false).descriptorStyle(NDescriptorStyle.MAVEN);
    }

    /**
     * Creates a new instance of of ntf nuts.
     *
     * @return of ntf nuts result
     */
    static NDescriptorWriter ofNtfNuts() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(true).descriptorStyle(NDescriptorStyle.NUTS ).ntf(true).descriptor style(n descriptor style.nuts
         * @return of result
         */
        return of().ntf(true).descriptorStyle(NDescriptorStyle.NUTS);
    }

    /**
     * Creates a new instance of of nuts.
     *
     * @return of nuts result
     */
    static NDescriptorWriter ofNuts() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).descriptorStyle(NDescriptorStyle.NUTS ).ntf(false).descriptor style(n descriptor style.nuts
         * @return of result
         */
        return of().ntf(false).descriptorStyle(NDescriptorStyle.NUTS);
    }

    /**
     * Creates a new instance of of ntf manifest.
     *
     * @return of ntf manifest result
     */
    static NDescriptorWriter ofNtfManifest() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(true).descriptorStyle(NDescriptorStyle.MANIFEST ).ntf(true).descriptor style(n descriptor style.manifest
         * @return of result
         */
        return of().ntf(true).descriptorStyle(NDescriptorStyle.MANIFEST);
    }

    /**
     * Creates a new instance of of manifest.
     *
     * @return of manifest result
     */
    static NDescriptorWriter ofManifest() {
        /**
         * Creates a new instance of of.
         *
         * @param ).ntf(false).descriptorStyle(NDescriptorStyle.MANIFEST ).ntf(false).descriptor style(n descriptor style.manifest
         * @return of result
         */
        return of().ntf(false).descriptorStyle(NDescriptorStyle.MANIFEST);
    }

    /**
     * Descriptor style.
     *
     * @return descriptor style result
     */
    NDescriptorStyle descriptorStyle();

    /**
     * Descriptor style.
     *
     * @param descriptorStyle descriptor style
     * @return descriptor style result
     */
    NDescriptorWriter descriptorStyle(NDescriptorStyle descriptorStyle);

    /**
     * true if compact flag is armed.
     * When true, formatted Descriptor will compact JSON result.
     *
     * @return true if compact flag is armed
     */
    boolean isCompact();

    /**
     * value compact flag.
     * When true, formatted Descriptor will compact JSON result.
     *
     * @param compact compact value
     * @return {@code this} instance
     */
    NDescriptorWriter compact(boolean compact);

    /**
     * value compact flag to true.
     * When true, formatted Descriptor will compact JSON result.
     *
     * @return {@code this} instance
     */
    NDescriptorWriter compact();


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
    NDescriptorWriter configure(boolean skipUnsupported, String... args);

    /**
     * Ntf.
     *
     * @param ntf ntf
     * @return ntf result
     */
    NDescriptorWriter ntf(boolean ntf);
}

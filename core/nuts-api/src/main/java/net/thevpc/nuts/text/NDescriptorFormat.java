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

import net.thevpc.nuts.artifact.NDescriptor;
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
public interface NDescriptorFormat extends NFormat, NComponent {
    static NDescriptorFormat ofNtf(NDescriptor value) {
        return of().setNtf(true).setValue(value);
    }

    static NDescriptorFormat ofPlain(NDescriptor value) {
        return of().setNtf(false).setValue(value);
    }

    static NDescriptorFormat of(NDescriptor value) {
        return of().setValue(value);
    }

    static NDescriptorFormat of() {
        return NExtensions.of(NDescriptorFormat.class);
    }

    static NDescriptorFormat ofNtfMaven(NDescriptor value) {
        return of().setNtf(true).setDescriptorStyle(NDescriptorStyle.MAVEN).setValue(value);
    }

    static NDescriptorFormat ofMaven(NDescriptor value) {
        return of().setNtf(false).setDescriptorStyle(NDescriptorStyle.MAVEN).setValue(value);
    }

    static NDescriptorFormat ofNtfNuts(NDescriptor value) {
        return of().setNtf(true).setDescriptorStyle(NDescriptorStyle.NUTS).setValue(value);
    }

    static NDescriptorFormat ofNuts(NDescriptor value) {
        return of().setNtf(false).setDescriptorStyle(NDescriptorStyle.NUTS).setValue(value);
    }

    static NDescriptorFormat ofNtfManifest(NDescriptor value) {
        return of().setNtf(true).setDescriptorStyle(NDescriptorStyle.MANIFEST).setValue(value);
    }

    static NDescriptorFormat ofManifest(NDescriptor value) {
        return of().setNtf(false).setDescriptorStyle(NDescriptorStyle.MANIFEST).setValue(value);
    }

    NDescriptorStyle getDescriptorStyle();

    NDescriptorFormat setDescriptorStyle(NDescriptorStyle descriptorStyle);

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
    NDescriptorFormat setCompact(boolean compact);

    /**
     * value compact flag.
     * When true, formatted Descriptor will compact JSON result.
     *
     * @param compact compact value
     * @return {@code this} instance
     */
    NDescriptorFormat compact(boolean compact);

    /**
     * value compact flag to true.
     * When true, formatted Descriptor will compact JSON result.
     *
     * @return {@code this} instance
     */
    NDescriptorFormat compact();

    /**
     * set the descriptor instance to print
     *
     * @param descriptor value to format
     * @return {@code this} instance
     * @since 0.5.6
     */
    NDescriptorFormat setValue(NDescriptor descriptor);

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
    NDescriptorFormat configure(boolean skipUnsupported, String... args);

    NDescriptorFormat setNtf(boolean ntf);
}

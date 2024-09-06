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

/**
 * Descriptor Format class that help building, formatting and parsing Descriptors.
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.4
 */
public interface NDescriptorFormat extends NFormat, NComponent {
    static NDescriptorFormat of(NSession session) {
       return NExtensions.of(session).createComponent(NDescriptorFormat.class).get();
    }

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

    NDescriptorFormat setSession(NSession session);

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

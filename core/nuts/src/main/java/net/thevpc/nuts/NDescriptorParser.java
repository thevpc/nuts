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

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * Descriptor Parser
 *
 * @author thevpc
 * @app.category Descriptor
 */
public interface NDescriptorParser extends NComponent, NSessionProvider {

    static NDescriptorParser of(NSession session) {
        return NExtensions.of(session).createComponent(NDescriptorParser.class).get();
    }

    /**
     * parse descriptor.
     *
     * @param url URL to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NOptional<NDescriptor> parse(URL url);

    /**
     * parse descriptor.
     *
     * @param bytes value to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NOptional<NDescriptor> parse(byte[] bytes);

    /**
     * parse descriptor.
     *
     * @param path path to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NOptional<NDescriptor> parse(Path path);

    /**
     * parse descriptor.
     *
     * @param file file to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NOptional<NDescriptor> parse(File file);

    /**
     * parse descriptor.
     *
     * @param stream stream to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NOptional<NDescriptor> parse(InputStream stream);

    NOptional<NDescriptor> parse(NPath path);

    /**
     * parse descriptor.
     *
     * @param descriptorString string to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NOptional<NDescriptor> parse(String descriptorString);

    NDescriptorStyle getDescriptorStyle();

    NDescriptorParser setDescriptorStyle(NDescriptorStyle descriptorStyle);

    NDescriptorParser setSession(NSession session);

}

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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * Descriptor Format class that help building, formatting and parsing Descriptors.
 * @author vpc
 * @since 0.5.4
 * @category Format
 */
public interface NutsDescriptorFormat extends NutsFormat {

    /**
     * true if compact flag is armed.
     * When true, formatted Descriptor will compact JSON result.
     * @return true if compact flag is armed
     */
    boolean isCompact();

    /**
     * value compact flag.
     * When true, formatted Descriptor will compact JSON result.
     * @param compact compact value
     * @return {@code this} instance
     */
    NutsDescriptorFormat compact(boolean compact);

    /**
     * value compact flag to true.
     * When true, formatted Descriptor will compact JSON result.
     * @return {@code this} instance
     */
    NutsDescriptorFormat compact();

    /**
     * value compact flag.
     * When true, formatted Descriptor will compact JSON result.
     * @param compact compact value
     * @return {@code this} instance
     */
    NutsDescriptorFormat setCompact(boolean compact);

    /**
     * set the descriptor instance to print
     *
     * @param descriptor value to format
     * @return {@code this} instance
     * @since 0.5.6
     */
    NutsDescriptorFormat value(NutsDescriptor descriptor);

    /**
     * parse descriptor.
     *
     * @param url URL to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(URL url);

    /**
     * parse descriptor.
     *
     * @param bytes value to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(byte[] bytes);

    /**
     * parse descriptor.
     *
     * @param path path to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(Path path);

    /**
     * parse descriptor.
     *
     * @param file file to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(File file);

    /**
     * parse descriptor.
     *
     * @param stream stream to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(InputStream stream);

    /**
     * parse descriptor.
     *
     * @param descriptorString string to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(String descriptorString);

    /**
     * create descriptor builder.
     * @return new instance of NutsDescriptorBuilder
     */
    NutsDescriptorBuilder descriptorBuilder();

    /**
     * create classifier mappings builder.
     * @return new instance of NutsClassifierMappingBuilder
     */
    NutsClassifierMappingBuilder classifierBuilder();

    /**
     * create descriptor builder.
     * @return new instance of NutsIdLocationBuilder
     */
    NutsIdLocationBuilder locationBuilder();

    /**
     * create executor builder.
     * @return new instance of NutsExecutorDescriptorBuilder
     */
    NutsArtifactCallBuilder callBuilder();

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
    NutsDescriptorFormat configure(boolean skipUnsupported, String... args);

}

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
package net.vpc.app.nuts.core.parsers;

import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsDescriptorContentParserComponent;
import net.vpc.app.nuts.NutsDescriptorContentParserContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.vpc.app.nuts.core.util.io.ZipUtils;

/**
 * Created by vpc on 1/15/17.
 */
public class ZipNutsDescriptorContentParserComponent implements NutsDescriptorContentParserComponent {

    public static final Set<String> POSSIBLE_PATHS = new LinkedHashSet<>(Arrays.asList("nuts.json", "/META-INF/nuts.json", "/WEB-INF/nuts.json", "/APP-INF/nuts.json"));
    public static final Set<String> POSSIBLE_EXT = new HashSet<>(Arrays.asList("zip", "gzip", "gz"));

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NutsDescriptor parse(NutsDescriptorContentParserContext parserContext) {
        if (!POSSIBLE_EXT.contains(parserContext.getFileExtension())) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            if (ZipUtils.extractFirstPath(parserContext.getFullStream(), POSSIBLE_PATHS, buffer, true)) {
                return parserContext.getWorkspace().parse().descriptor(buffer.toByteArray());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return null;
    }
}

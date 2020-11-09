/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
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
package net.thevpc.nuts.runtime.main.parsers;

import net.thevpc.nuts.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import net.thevpc.nuts.runtime.util.io.ZipUtils;

/**
 * Created by vpc on 1/15/17.
 */
@NutsSingleton
public class ZipNutsDescriptorContentParserComponent implements NutsDescriptorContentParserComponent {

    public static final Set<String> POSSIBLE_PATHS = new LinkedHashSet<>(Arrays.asList(
            NutsConstants.Files.DESCRIPTOR_FILE_NAME,
            "/META-INF/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME,
            "/WEB-INF/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME,
            "/APP-INF/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME
    ));
    public static final Set<String> POSSIBLE_EXT = new HashSet<>(Arrays.asList("zip", "gzip", "gz"));

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
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
                return parserContext.getWorkspace().descriptor().parser().parse(buffer.toByteArray());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return null;
    }
}

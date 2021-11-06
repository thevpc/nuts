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
package net.thevpc.nuts.runtime.standalone.parsers;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.ZipUtils;
import net.thevpc.nuts.spi.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by vpc on 1/15/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class ZipDescriptorContentParserComponent implements NutsDescriptorContentParserComponent {

    public static final Set<String> POSSIBLE_PATHS = new LinkedHashSet<>(Arrays.asList(
            NutsConstants.Files.DESCRIPTOR_FILE_NAME,
            "META-INF/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME,
            "WEB-INF/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME,
            "APP-INF/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME
    ));
    public static final Set<String> POSSIBLE_EXT = new HashSet<>(Arrays.asList("zip", "gzip", "gz","war","ear"));

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        NutsDescriptorContentParserContext constraints = criteria.getConstraints(NutsDescriptorContentParserContext.class);
        if(constraints!=null) {
            String e = NutsUtilStrings.trim(constraints.getFileExtension());
            if (!POSSIBLE_EXT.contains(e)) {
                return NO_SUPPORT;
            }
        }
        return NO_SUPPORT;
    }

    @Override
    public NutsDescriptor parse(NutsDescriptorContentParserContext parserContext) {
        String e = NutsUtilStrings.trim(parserContext.getFileExtension());
        if (!POSSIBLE_EXT.contains(e)) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            if (ZipUtils.extractFirstPath(parserContext.getFullStream(), POSSIBLE_PATHS, buffer, true)) {
                NutsSession session = parserContext.getSession();
                return NutsDescriptorParser.of(session)
                        .parse(buffer.toByteArray());
            }
        } catch (IOException ex) {
            throw new NutsIOException(parserContext.getSession(),ex);
        }
        return null;
    }
}

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
package net.thevpc.nuts.runtime.standalone.xtra.contenttype;

import net.thevpc.nuts.spi.NutsContentTypeResolver;
import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsSupported;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultNutsContentTypeResolver implements NutsContentTypeResolver {

    public NutsSupported<String> probeContentType(NutsPath path, NutsSession session) {
        String contentType = null;
        if (path != null) {
            if (path.isFile()) {
                Path file = path.asFile();
                if (file != null) {
                    try {
                        contentType = Files.probeContentType(file);
                    } catch (IOException e) {
                        //ignore
                    }
                }
            }
            if (contentType == null) {
                URL url = path.asURL();
                try {
                    contentType = url.openConnection().getContentType();
                } catch (IOException e) {
                    //
                }
            }

            if (contentType == null) {
                String name=path.getName();
                try {
                    contentType = URLConnection.guessContentTypeFromName(name);
                } catch (Exception e) {
                    //ignore
                }
                if (contentType == null || "text/plain".equals(contentType)) {
                    String e = NutsPath.of(Paths.get(name), session).getLastExtension();
                    if (e != null && e.equalsIgnoreCase("ntf")) {
                        return NutsSupported.of(DEFAULT_SUPPORT + 10, "text/x-nuts-text-format");
                    }
                }
                if (contentType == null || "text/plain".equals(contentType)) {
                    String e = NutsPath.of(Paths.get(name), session).getLastExtension();
                    if (e != null && e.equalsIgnoreCase("nuts")) {
                        return NutsSupported.of(DEFAULT_SUPPORT + 10, "application/json");
                    }
                }
            }
            if (contentType != null) {
                return NutsSupported.of(DEFAULT_SUPPORT, contentType);
            }
        }

        return NutsSupported.invalid();
    }


    @Override
    public NutsSupported<String> probeContentType(byte[] bytes, NutsSession session) {
        String contentType = null;
        if (bytes != null) {
            try {
                contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                //ignore
            }
        }
        if (contentType != null) {
            return NutsSupported.of(DEFAULT_SUPPORT, contentType);
        }
        return NutsSupported.invalid();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}


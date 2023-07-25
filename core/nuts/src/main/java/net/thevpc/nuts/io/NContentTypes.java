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
package net.thevpc.nuts.io;

import net.thevpc.nuts.NExtensions;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponent;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/**
 * Probing content types (or mime types) from paths and contents.
 * Probing is highly dependent on the underlying system and user configuration
 *
 * @author thevpc
 * @since 0.8.3
 */
public interface NContentTypes extends NComponent {
    static NContentTypes of(NSession session) {
       return NExtensions.of(session).createComponent(NContentTypes.class).get();
    }

    String probeContentType(URL path);

    String probeContentType(File path);

    String probeContentType(Path path);

    String probeContentType(NPath path);

    String probeContentType(InputStream stream);

    String probeContentType(byte[] stream);

    ////////
    String probeCharset(URL path);

    String probeCharset(File path);

    String probeCharset(Path path);

    String probeCharset(NPath path);

    String probeCharset(InputStream stream);

    String probeCharset(byte[] stream);

    List<String> findExtensionsByContentType(String contentType);
    List<String> findContentTypesByExtension(String extension);
}

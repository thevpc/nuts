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
package net.thevpc.nuts.runtime.standalone.descriptor;

import net.thevpc.nuts.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsDescriptorContentParserContext;

/**
 * Created by vpc on 1/29/17.
 */
public class DefaultNutsDescriptorContentParserContext implements NutsDescriptorContentParserContext {

    private final NutsSession session;
    private final NutsPath file;
    private final String fileExtension;
    private final String mimeType;
    private byte[] bytes;
    private final List<String> parseOptions;

    public DefaultNutsDescriptorContentParserContext(NutsSession session, Path file, String fileExtension, String mimeType, List<String> parseOptions) {
        this.file = NutsPath.of(file, session);
        this.session = session;
        this.fileExtension = fileExtension;
        this.mimeType = mimeType;
        this.parseOptions = parseOptions;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public List<String> getParseOptions() {
        return parseOptions;
    }

    @Override
    public InputStream getHeadStream() {
        if (bytes == null) {
            try {
                try (InputStream is = file.getInputStream()) {
                    bytes = CoreIOUtils.loadByteArray(is, 1024 * 1024 * 10, true, session);
                }
            } catch (IOException e) {
                throw new NutsIOException(session, e);
            }
        }
        return CoreIOUtils.createBytesStream(bytes, NutsMessage.ofCstyle("%s", file), file.getContentType(),
                file.getOutputMetaData().getKind().orNull(), session);
    }

    @Override
    public InputStream getFullStream() {
        return file.getInputStream();
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String getName() {
        return file.getName();
    }

}

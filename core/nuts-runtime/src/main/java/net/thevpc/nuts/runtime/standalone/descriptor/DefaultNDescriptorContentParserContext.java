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
package net.thevpc.nuts.runtime.standalone.descriptor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.NDescriptorContentParserContext;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.util.NMsg;

/**
 * Created by vpc on 1/29/17.
 */
public class DefaultNDescriptorContentParserContext implements NDescriptorContentParserContext {

    private final NPath file;
    private final String fileExtension;
    private final String mimeType;
    private byte[] bytes;
    private final List<String> parseOptions;

    public DefaultNDescriptorContentParserContext(Path file, String fileExtension, String mimeType, List<String> parseOptions) {
        this.file = NPath.of(file);
        this.fileExtension = fileExtension;
        this.mimeType = mimeType;
        this.parseOptions = parseOptions;
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
                    bytes = NIOUtils.loadByteArray(is, 1024 * 1024 * 10, true);
                }
            } catch (IOException e) {
                throw new NIOException(e);
            }
        }
        return CoreIOUtils.createBytesStream(bytes, NMsg.ofC("%s", file), file.getContentType(),
                file.getCharset(), file.getMetaData().getKind().orNull());
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

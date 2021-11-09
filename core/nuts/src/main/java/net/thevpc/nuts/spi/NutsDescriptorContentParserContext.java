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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

import java.io.InputStream;

/**
 * context holding useful information for {@link NutsDescriptorContentParserComponent#parse(NutsDescriptorContentParserContext)}
 *
 * @app.category SPI Base
 * @since 0.5.4
 */
public interface NutsDescriptorContentParserContext {

    /**
     * command line options that can be parsed to
     * configure parsing options.
     * A good example of it is the --all-mains option that can be passed
     * as executor option which will be catched by parser to force resolution
     * of all main classes even though a Main-Class attribute is visited in the MANIFEST.MF
     * file.
     * This array may continue any non supported options. They should be discarded by the parser.
     *
     * @return parser options.
     * @since 0.5.8
     */
    String[] getParseOptions();

    /**
     * return content header stream.
     * if the content size is less than 1Mb, then all the content is returned.
     * If not, at least 1Mb is returned.
     *
     * @return content header stream
     */
    InputStream getHeadStream();

    /**
     * content stream
     *
     * @return content stream
     */
    InputStream getFullStream();

    /**
     * content file extension or null. At least one of file extension or file mime-type is provided.
     *
     * @return content file extension
     */
    String getFileExtension();

    /**
     * content mime-type or null. At least one of file extension or file mime-type is provided.
     *
     * @return content file extension
     */
    String getMimeType();

    /**
     * content name (mostly content file name)
     *
     * @return content name (mostly content file name)
     */
    String getName();

    /**
     * return workspace
     *
     * @return workspace
     */
    NutsWorkspace getWorkspace();

    /**
     * return session
     *
     * @return session
     */
    NutsSession getSession();

}

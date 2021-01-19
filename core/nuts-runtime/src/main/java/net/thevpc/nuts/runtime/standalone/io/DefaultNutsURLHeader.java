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
package net.thevpc.nuts.runtime.standalone.io;

import java.time.Instant;
import net.thevpc.nuts.NutsURLHeader;
import net.thevpc.nuts.runtime.bundles.http.SimpleHttpClient;

/**
 *
 * @author thevpc
 * @since 0.5.4
 */
public class DefaultNutsURLHeader implements NutsURLHeader {

    public static final long serialVersionUID = 1;
    private String url;
    private long contentLength;
    private String contentType;
    private String contentEncoding;
    private Instant lastModified;

    public DefaultNutsURLHeader(SimpleHttpClient url) {
        this.url = url.getURL().toString();
        this.contentLength=url.getContentLength();
        this.contentType=url.getContentType();
        this.contentEncoding=url.getContentEncoding();
        this.lastModified=url.getLastModified();
    }

    public DefaultNutsURLHeader(String url) {
        this.url = url;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public DefaultNutsURLHeader setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    public DefaultNutsURLHeader setContentLength(long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public DefaultNutsURLHeader setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public String getContentEncoding() {
        return contentEncoding;
    }

    public DefaultNutsURLHeader setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    @Override
    public Instant getLastModified() {
        return lastModified;
    }

    public DefaultNutsURLHeader setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
        return this;
    }

}

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
package net.thevpc.nuts.runtime.standalone.xtra.download;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.SimpleHttpClient;
import net.thevpc.nuts.spi.*;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by vpc on 1/21/17.
 */
@NComponentScope(NScopeType.SESSION)
public class DefaultHttpTransportComponent implements NTransportComponent {

    public static final NTransportComponent INSTANCE = new DefaultHttpTransportComponent();
    private NSession session;
    @Override
    public int getSupportLevel(NSupportLevelContext url) {
        session=url.getSession();
        return NCallableSupport.DEFAULT_SUPPORT;
    }

    @Override
    public NTransportConnection open(String url) throws UncheckedIOException {
        try {
            return new DefaultNTransportConnection(new URL(url),session);
        } catch (MalformedURLException e) {
            throw new NIOException(session,e);
        }
    }

    private static class DefaultNTransportConnection implements NTransportConnection {

        private final URL url;
        private final NSession session;

        public DefaultNTransportConnection(URL url, NSession session) {
            this.url = url;
            this.session = session;
        }

        @Override
        public InputStream open() {
            return new SimpleHttpClient(url, session).openStream();
        }

        @Override
        public NPath getPath() {
            return NPath.of(url,session);
        }

        public InputStream upload(NTransportParamPart... parts) {
            throw new NUnsupportedOperationException(session, NMsg.ofPlain("upload unsupported"));
        }

        @Override
        public String toString() {
            return String.valueOf(url);
        }
        
    }
}

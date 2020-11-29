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
package net.thevpc.nuts.runtime.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import net.thevpc.nuts.spi.NutsTransportComponent;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by vpc on 1/21/17.
 */
@NutsSingleton
public class DefaultHttpTransportComponent implements NutsTransportComponent {

    public static final NutsTransportComponent INSTANCE = new DefaultHttpTransportComponent();

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> url) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NutsTransportConnection open(String url) throws UncheckedIOException {
        try {
            return new DefaultNutsTransportConnection(new URL(url));
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class DefaultNutsTransportConnection implements NutsTransportConnection {

        private final URL url;

        public DefaultNutsTransportConnection(URL url) {
            this.url = url;
        }

        @Override
        public InputStream open() {
            try {
                return url.openStream();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        @Override
        public NutsURLHeader getURLHeader() {
            return CoreIOUtils.getURLHeader(url);
        }

        public InputStream upload(NutsTransportParamPart... parts) {
            throw new NutsUnsupportedOperationException(null, "Upload unsupported");
        }

        @Override
        public String toString() {
            return String.valueOf(url);
        }
        
    }
}

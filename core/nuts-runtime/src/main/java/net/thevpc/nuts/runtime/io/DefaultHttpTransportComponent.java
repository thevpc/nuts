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
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts.runtime.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;

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

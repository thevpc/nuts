/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.boot;

import net.vpc.app.nuts.HttpConnectionFacade;
import net.vpc.app.nuts.NutsTransportComponent;
import net.vpc.app.nuts.TransportParamPart;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/21/17.
 */
public class DefaultHttpTransportComponent implements NutsTransportComponent {
    public static final NutsTransportComponent INSTANCE = new DefaultHttpTransportComponent();
    private static final Logger log = Logger.getLogger(DefaultHttpTransportComponent.class.getName());

    @Override
    public int getSupportLevel(String url) {
        return BOOT_SUPPORT;
    }

    @Override
    public HttpConnectionFacade open(String url) throws IOException {
        return new DefaultHttpConnectionFacade(new URL(url));
    }

    private static class DefaultHttpConnectionFacade implements HttpConnectionFacade {

        private URL url;

        public DefaultHttpConnectionFacade(URL url) {
            this.url = url;
        }

        @Override
        public InputStream open() throws IOException {
            return url.openStream();
        }

        public InputStream upload(TransportParamPart... parts) throws IOException {
            throw new IOException("Upload unsupported");
        }
    }
}

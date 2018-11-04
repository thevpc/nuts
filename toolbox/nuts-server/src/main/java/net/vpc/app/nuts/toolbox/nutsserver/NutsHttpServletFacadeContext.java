/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nutsserver;

import net.vpc.common.util.ListMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Created by vpc on 1/7/17.
 */
public interface NutsHttpServletFacadeContext {

    URI getRequestURI() throws IOException;

    OutputStream getResponseBody() throws IOException;

    void sendError(int code, String msg) throws IOException;

    void sendResponseHeaders(int code, long length) throws IOException;

    void sendResponseText(int code, String text) throws IOException;

    void sendResponseFile(int code, File file) throws IOException;

    Set<String> getRequestHeaderKeys(String header) throws IOException;

    String getRequestHeaderFirstValue(String header) throws IOException;

    List<String> getRequestHeaderAllValues(String header) throws IOException;

    InputStream getRequestBody() throws IOException;

    ListMap<String, String> getParameters() throws IOException;

}

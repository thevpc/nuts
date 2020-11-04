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
 * Copyright (C) 2016-2020 thevpc
 * <br>
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
package net.thevpc.nuts.toolbox.nutsserver.http;

import net.thevpc.common.util.ListValueMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Created by vpc on 1/7/17.
 */
public interface NutsHttpServletFacadeContext {

    String getRequestMethod() throws IOException;

    boolean isGetMethod() throws IOException;

    boolean isPostMethod() throws IOException;

    boolean isHeadMethod() throws IOException;

    URI getRequestURI() throws IOException;

    OutputStream getResponseBody() throws IOException;

    void sendError(int code, String msg) throws IOException;

    void sendResponseHeaders(int code, long length) throws IOException;

    void sendResponseText(int code, String text) throws IOException;

    void sendResponseFile(int code, File file) throws IOException;

    void sendResponseBytes(int code, byte[] bytes) throws IOException;

    void sendResponseFile(int code, Path file) throws IOException;

    Set<String> getRequestHeaderKeys(String header) throws IOException;

    String getRequestHeaderFirstValue(String header) throws IOException;

    List<String> getRequestHeaderAllValues(String header) throws IOException;

    InputStream getRequestBody() throws IOException;

    ListValueMap<String, String> getParameters() throws IOException;

    void addResponseHeader(String name, String value) throws IOException;

}

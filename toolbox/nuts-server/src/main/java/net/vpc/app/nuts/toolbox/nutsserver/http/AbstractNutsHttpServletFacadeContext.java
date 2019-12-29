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
package net.vpc.app.nuts.toolbox.nutsserver.http;

import net.vpc.app.nuts.toolbox.nutsserver.http.NutsHttpServletFacadeContext;
import net.vpc.common.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by vpc on 1/7/17.
 */
public abstract class AbstractNutsHttpServletFacadeContext implements NutsHttpServletFacadeContext {

    public void sendResponseText(int code, String text) throws IOException {
        byte[] bytes = text.getBytes();
        sendResponseHeaders(code, bytes.length);
        getResponseBody().write(bytes);
    }

    public void sendResponseFile(int code, File file) throws IOException {
        if (file != null && file.exists() && file.isFile()) {
            sendResponseHeaders(code, file.length());
            IOUtils.copy(new FileInputStream(file), getResponseBody(), true, false);
        } else {
            sendError(404, "File not found");
        }
    }

    public void sendResponseBytes(int code, byte[] bytes) throws IOException {
        sendResponseHeaders(code, bytes.length);
        getResponseBody().write(bytes);
    }

    @Override
    public void sendResponseFile(int code, Path file) throws IOException {
        if (file != null && Files.isRegularFile(file)) {
            sendResponseHeaders(code, Files.size(file));
            Files.copy(file, getResponseBody());
        } else {
            sendError(404, "File not found");
        }
    }

    @Override
    public boolean isGetMethod() throws IOException {
        return "GET".equalsIgnoreCase(getRequestMethod());
    }

    @Override
    public boolean isPostMethod() throws IOException {
        return "POST".equalsIgnoreCase(getRequestMethod());
    }

    @Override
    public boolean isHeadMethod() throws IOException {
        return "HEAD".equalsIgnoreCase(getRequestMethod());
    }
}

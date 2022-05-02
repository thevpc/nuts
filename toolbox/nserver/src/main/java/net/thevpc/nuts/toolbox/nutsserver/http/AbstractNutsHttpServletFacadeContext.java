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
package net.thevpc.nuts.toolbox.nutsserver.http;

import net.thevpc.nuts.io.NutsCp;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.toolbox.nutsserver.bundled._IOUtils;

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
            _IOUtils.copy(new FileInputStream(file), getResponseBody(), true, false);
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
    public void sendResponseFile(int code, NutsPath file) throws IOException {
        if (file != null && file.isRegularFile()) {
            sendResponseHeaders(code, file.getContentLength());
            NutsCp.of(file.getSession()).from(file).to(getResponseBody()).run();
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

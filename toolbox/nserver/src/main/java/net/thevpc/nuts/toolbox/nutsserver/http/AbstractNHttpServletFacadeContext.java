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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nutsserver.http;

import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.nutsserver.bundled._IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by vpc on 1/7/17.
 */
public abstract class AbstractNHttpServletFacadeContext implements NHttpServletFacadeContext {

    public void sendResponseText(int code, String text) {
        byte[] bytes = text.getBytes();
        sendResponseHeaders(code, bytes.length);
        try {
            getResponseBody().write(bytes);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public void sendResponseFile(int code, File file) {
        if (file != null && file.exists() && file.isFile()) {
            sendResponseHeaders(code, file.length());
            try {
                _IOUtils.copy(new FileInputStream(file), getResponseBody(), true, false);
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        } else {
            sendError(404, "File not found");
        }
    }

    public void sendResponseBytes(int code, byte[] bytes) {
        sendResponseHeaders(code, bytes.length);
        try {
            getResponseBody().write(bytes);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    @Override
    public void sendResponseFile(int code, Path file) {
        if (file != null && Files.isRegularFile(file)) {
            try {
                sendResponseHeaders(code, Files.size(file));
                Files.copy(file, getResponseBody());
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        } else {
            sendError(404, "File not found");
        }
    }

    @Override
    public void sendResponseFile(int code, NPath file) {
        if (file != null && file.isRegularFile()) {
            sendResponseHeaders(code, file.getContentLength());
            NCp.of().from(file).to(getResponseBody()).run();
        } else {
            sendError(404, "File not found");
        }
    }

    @Override
    public boolean isGetMethod() {
        return "GET".equalsIgnoreCase(getRequestMethod());
    }

    @Override
    public boolean isPostMethod() {
        return "POST".equalsIgnoreCase(getRequestMethod());
    }

    @Override
    public boolean isHeadMethod() {
        return "HEAD".equalsIgnoreCase(getRequestMethod());
    }
}

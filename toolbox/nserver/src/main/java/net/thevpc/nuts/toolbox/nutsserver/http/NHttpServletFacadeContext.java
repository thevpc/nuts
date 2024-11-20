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

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by vpc on 1/7/17.
 */
public interface NHttpServletFacadeContext {

    String getRequestMethod() throws NIOException;

    boolean isGetMethod() throws NIOException;

    boolean isPostMethod() throws NIOException;

    boolean isHeadMethod() throws NIOException;

    URI getRequestURI() throws NIOException;

    OutputStream getResponseBody() throws NIOException;

    void sendError(int code, String msg) throws NIOException;

    void sendResponseHeaders(int code, long length) throws NIOException;

    void sendResponseText(int code, String text) throws NIOException;

    void sendResponseFile(int code, File file) throws NIOException;

    void sendResponseBytes(int code, byte[] bytes) throws NIOException;

    void sendResponseFile(int code, NPath file) throws NIOException;

    void sendResponseFile(int code, Path file) throws NIOException;

    Set<String> getRequestHeaderKeys(String header) throws NIOException;

    String getRequestHeaderFirstValue(String header) throws NIOException;

    List<String> getRequestHeaderAllValues(String header) throws NIOException;

    InputStream getRequestBody() throws NIOException;

    Map<String, List<String>> getParameters() throws NIOException;

    void addResponseHeader(String name, String value) throws NIOException;

}

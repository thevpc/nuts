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

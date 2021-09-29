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
package net.thevpc.nuts.toolbox.nutsserver;

import net.thevpc.nuts.toolbox.nutsserver.http.NutsHttpServletFacadeContext;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

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
 * Created by vpc on 1/24/17.
 */
public class FacadeCommandContext implements NutsHttpServletFacadeContext {

    private NutsHttpServletFacadeContext base;
    private NutsWorkspace workspace;
    private String serverId;
    private String command;
    private String path;
    private NutsSession session;

    public FacadeCommandContext(NutsHttpServletFacadeContext base, String serverId, String command, String path, NutsSession session) {
        this.base = base;
        this.workspace = session.getWorkspace();
        this.serverId = serverId;
        this.command = command;
        this.path = path;
        this.session = session;
    }

    public NutsSession getSession() {
        return session;
    }

    public String getPath() {
        return path;
    }

    public String getCommand() {
        return command;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public String getServerId() {
        return serverId;
    }

    @Override
    public URI getRequestURI() throws IOException {
        return base.getRequestURI();
    }

    @Override
    public OutputStream getResponseBody() throws IOException {
        return base.getResponseBody();
    }

    @Override
    public void sendResponseHeaders(int code, long length) throws IOException {
        base.sendResponseHeaders(code, length);
    }

    @Override
    public void sendError(int code, String msg) throws IOException {
        base.sendError(code, msg);
    }

    @Override
    public void sendResponseText(int code, String text) throws IOException {
        base.sendResponseText(code, text);
    }

    @Override
    public void sendResponseFile(int code, File file) throws IOException {
        base.sendResponseFile(code, file);
    }

    public void sendResponseBytes(int code, byte[] bytes) throws IOException {
        base.sendResponseBytes(code, bytes);
    }

    @Override
    public void sendResponseFile(int code, Path file) throws IOException {
        base.sendResponseFile(code, file);
    }

    @Override
    public Set<String> getRequestHeaderKeys(String header) throws IOException {
        return base.getRequestHeaderKeys(header);
    }

    @Override
    public String getRequestHeaderFirstValue(String header) throws IOException {
        return base.getRequestHeaderFirstValue(header);
    }

    @Override
    public List<String> getRequestHeaderAllValues(String header) throws IOException {
        return base.getRequestHeaderAllValues(header);
    }

    @Override
    public InputStream getRequestBody() throws IOException {
        return base.getRequestBody();
    }

    @Override
    public Map<String, List<String>> getParameters() throws IOException {
        return base.getParameters();
    }

    @Override
    public void addResponseHeader(String name, String value) throws IOException {
        base.addResponseHeader(name, value);
    }

    @Override
    public String getRequestMethod() throws IOException {
        return base.getRequestMethod();
    }

    @Override
    public boolean isGetMethod() throws IOException {
        return base.isGetMethod();
    }

    @Override
    public boolean isPostMethod() throws IOException {
        return base.isPostMethod();
    }

    @Override
    public boolean isHeadMethod() throws IOException {
        return base.isHeadMethod();
    }
}

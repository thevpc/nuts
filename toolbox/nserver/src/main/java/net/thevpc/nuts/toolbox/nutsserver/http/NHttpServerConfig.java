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
 * <br>
 *
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

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.toolbox.nutsserver.ServerConfig;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by vpc on 1/23/17.
 */
public final class NHttpServerConfig extends ServerConfig {

    private String serverId;
    private InetAddress address;
    private int port;
    private int backlog;
    private Executor executor;
    private int executorCorePoolSize=-1;
    private int executorMaximumPoolSize=-1;
    private int executorQueueSize=-1;
    private int executorIdleTimeSeconds=-1;
    private boolean tls;
    private byte[] sslKeystoreCertificate;
    private char[] sslKeystorePassphrase;
    private Map<String, NWorkspace> workspaces = new HashMap<>();

    public NHttpServerConfig setWorkspaces(Map<String, NWorkspace> workspaces) {
        this.workspaces = workspaces;
        return this;
    }

    public String getServerId() {
        return serverId;
    }

    public NHttpServerConfig setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public Map<String, NWorkspace> getWorkspaces() {
        return workspaces;
    }

    public boolean isTls() {
        return tls;
    }

    public byte[] getSslKeystoreCertificate() {
        return sslKeystoreCertificate;
    }

    public NHttpServerConfig setSslKeystoreCertificate(byte[] sslKeystoreCertificate) {
        this.sslKeystoreCertificate = sslKeystoreCertificate;
        return this;
    }

    public char[] getSslKeystorePassphrase() {
        return sslKeystorePassphrase;
    }

    public NHttpServerConfig setSslKeystorePassphrase(char[] sslKeystorePassphrase) {
        this.sslKeystorePassphrase = sslKeystorePassphrase;
        return this;
    }

    public NHttpServerConfig setTls(boolean tls) {
        this.tls = tls;
        return this;
    }

    public InetAddress getAddress() {
        return address;
    }

    public NHttpServerConfig setAddress(InetAddress address) {
        this.address = address;
        return this;
    }

    public int getPort() {
        return port;
    }

    public NHttpServerConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public int getBacklog() {
        return backlog;
    }

    public NHttpServerConfig setBacklog(int backlog) {
        this.backlog = backlog;
        return this;
    }

    public Executor getExecutor() {
        return executor;
    }

    public NHttpServerConfig setExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public int getExecutorCorePoolSize() {
        return executorCorePoolSize;
    }

    public NHttpServerConfig setExecutorCorePoolSize(int executorCorePoolSize) {
        this.executorCorePoolSize = executorCorePoolSize;
        return this;
    }

    public int getExecutorMaximumPoolSize() {
        return executorMaximumPoolSize;
    }

    public NHttpServerConfig setExecutorMaximumPoolSize(int executorMaximumPoolSize) {
        this.executorMaximumPoolSize = executorMaximumPoolSize;
        return this;
    }

    public int getExecutorQueueSize() {
        return executorQueueSize;
    }

    public NHttpServerConfig setExecutorQueueSize(int executorQueueSize) {
        this.executorQueueSize = executorQueueSize;
        return this;
    }

    public int getExecutorIdleTimeSeconds() {
        return executorIdleTimeSeconds;
    }

    public NHttpServerConfig setExecutorIdleTimeSeconds(int executorIdleTimeSeconds) {
        this.executorIdleTimeSeconds = executorIdleTimeSeconds;
        return this;
    }
}

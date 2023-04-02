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
package net.thevpc.nuts.toolbox.ndb;

import java.util.Objects;

/**
 * @author thevpc
 */
public class NdbConfig implements Cloneable {

    private String name;
    private String user;
    private String password;
    private String databaseName;
    private String host;
    private Integer port;
    private Integer remotePort;
    private String remoteServer;
    private String remoteUser;
    private String remotePassword;
    private String remoteTempFolder;

    public NdbConfig setNonNull(NdbConfig other) {
        if (other != null) {
            if (other.getName() != null) {
                this.setName(other.getName());
            }
            if (other.getUser() != null) {
                this.setUser(other.getUser());
            }
            if (other.getPassword() != null) {
                this.setPassword(other.getPassword());
            }
            if (other.getDatabaseName() != null) {
                this.setDatabaseName(other.getDatabaseName());
            }
            if (other.getHost() != null) {
                this.setHost(other.getHost());
            }
            if (other.getPort() != null) {
                this.setPort(other.getPort());
            }
            if (other.getRemoteServer() != null) {
                this.setRemoteServer(other.getRemoteServer());
            }
            if (other.getRemoteUser() != null) {
                this.setRemoteUser(other.getRemoteUser());
            }
            if (other.getRemoteTempFolder() != null) {
                this.setRemoteTempFolder(other.getRemoteTempFolder());
            }
        }
        return this;
    }

    public String getRemoteServer() {
        return remoteServer;
    }

    public NdbConfig setRemoteServer(String remoteServer) {
        this.remoteServer = remoteServer;
        return this;
    }

    public String getRemoteTempFolder() {
        return remoteTempFolder;
    }

    public NdbConfig setRemoteTempFolder(String remoteTempFolder) {
        this.remoteTempFolder = remoteTempFolder;
        return this;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public NdbConfig setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
        return this;
    }

    public String getName() {
        return name;
    }

    public NdbConfig setName(String name) {
        this.name = name;
        return this;
    }

    public String getUser() {
        return user;
    }

    public NdbConfig setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public NdbConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public NdbConfig setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public String getHost() {
        return host;
    }

    public NdbConfig setHost(String host) {
        this.host = host;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public NdbConfig setPort(Integer port) {
        this.port = port;
        return this;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public NdbConfig setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
        return this;
    }

    public String getRemotePassword() {
        return remotePassword;
    }

    public NdbConfig setRemotePassword(String remotePassword) {
        this.remotePassword = remotePassword;
        return this;
    }

    public NdbConfig copy() {
        return clone();
    }

    @Override
    protected NdbConfig clone() {
        try {
            return (NdbConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NdbConfig that = (NdbConfig) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getUser(), that.getUser()) && Objects.equals(getPassword(), that.getPassword()) && Objects.equals(getDatabaseName(), that.getDatabaseName()) && Objects.equals(getHost(), that.getHost()) && Objects.equals(getPort(), that.getPort());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getUser(), getPassword(), getDatabaseName(), getHost(), getPort());
    }
}

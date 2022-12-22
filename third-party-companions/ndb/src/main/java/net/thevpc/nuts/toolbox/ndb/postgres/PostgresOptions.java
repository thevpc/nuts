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
package net.thevpc.nuts.toolbox.ndb.postgres;

import java.util.Objects;

/**
 * @author thevpc
 */
public class PostgresOptions implements Cloneable {

    public String name;
    public String user;
    public String password;
    public String databaseName;
    public String host;
    public Integer port;

    public PostgresOptions setNonNull(PostgresOptions other) {
        if (other != null) {
            if (other.name != null) {
                this.name = other.name;
            }
            if (other.user != null) {
                this.user = other.user;
            }
            if (other.password != null) {
                this.password = other.password;
            }
            if (other.databaseName != null) {
                this.databaseName = other.databaseName;
            }
            if (other.host != null) {
                this.host = other.host;
            }
            if (other.port != null) {
                this.port = other.port;
            }
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public PostgresOptions setName(String name) {
        this.name = name;
        return this;
    }

    public String getUser() {
        return user;
    }

    public PostgresOptions setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public PostgresOptions setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public PostgresOptions setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public String getHost() {
        return host;
    }

    public PostgresOptions setHost(String host) {
        this.host = host;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public PostgresOptions setPort(Integer port) {
        this.port = port;
        return this;
    }

    public PostgresOptions copy() {
        try {
            return (PostgresOptions) clone();
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostgresOptions that = (PostgresOptions) o;
        return Objects.equals(name, that.name) && Objects.equals(user, that.user) && Objects.equals(password, that.password) && Objects.equals(databaseName, that.databaseName) && Objects.equals(host, that.host) && Objects.equals(port, that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, user, password, databaseName, host, port);
    }
}

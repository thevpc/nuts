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
 * Copyright (C) 2016-2020 thevpc
 *
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
package net.thevpc.nuts.toolbox.nutsserver;

import java.net.InetAddress;
import java.util.concurrent.Executor;

/**
 * Created by vpc on 1/23/17.
 */
public final class AdminServerConfig extends ServerConfig {

    private String serverId;
    private InetAddress address;
    private int port;
    private int backlog;
    private Executor executor;

    public String getServerId() {
        return serverId;
    }

    public AdminServerConfig setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public InetAddress getAddress() {
        return address;
    }

    public AdminServerConfig setAddress(InetAddress address) {
        this.address = address;
        return this;
    }

    public int getPort() {
        return port;
    }

    public AdminServerConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public int getBacklog() {
        return backlog;
    }

    public AdminServerConfig setBacklog(int backlog) {
        this.backlog = backlog;
        return this;
    }

    public Executor getExecutor() {
        return executor;
    }

    public AdminServerConfig setExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }
}

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
package net.vpc.app.nuts;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by vpc on 1/5/17.
 */
public interface NutsRepository {

    int SPEED_FASTEST = 100;
    int SPEED_FAST = 1000;
    int SPEED_SLOW = 10000;
    int SPEED_SLOWEST = 100000;

    int getSpeed();

    String getEnv(String key, String defaultValue, boolean inherit);

    NutsRepositoryConfig getConfig();

    NutsWorkspace getWorkspace();

    String getRepositoryId();

    /**
     * @param id         descriptor Id, mandatory as descriptor may not being effective
     *                   (id is variable or inherited)
     * @param descriptor
     * @param file
     * @return
     * @throws IOException
     */
    NutsId deploy(NutsId id, NutsDescriptor descriptor, File file, NutsSession context) throws IOException;

    void push(NutsId id, String repoId, NutsSession session) throws IOException;

    File fetch(NutsId id, NutsSession session, File localPath) throws IOException;

    NutsFile fetch(NutsId id, NutsSession session) throws IOException;

    NutsDescriptor fetchDescriptor(NutsId id, NutsSession session) throws IOException;

    boolean fetchDescriptor(NutsId id, NutsSession session, File localPath) throws IOException;

    String fetchHash(NutsId id, NutsSession session) throws IOException;

    String fetchDescriptorHash(NutsId id, NutsSession session) throws IOException;

    NutsId resolveId(NutsId id, NutsSession session) throws IOException;

    Iterator<NutsId> find(NutsDescriptorFilter filter, NutsSession session) throws IOException;

    Iterator<NutsId> findVersions(NutsId id, NutsDescriptorFilter versionFilter, NutsSession session) throws IOException;

    Iterator<NutsId> findVersions(NutsId id, NutsVersionFilter versionFilter, NutsSession session) throws IOException;

    int getSupportLevel(NutsId id, NutsSession session);

    boolean isSupportedMirroring();

    NutsRepository[] getMirrors();

    NutsRepository getMirror(String repositoryIdPath);

    NutsRepository addMirror(String repositoryId, String location, String type, boolean autoCreate) throws IOException;

    void removeMirror(String repositoryId) throws IOException;

    void save() throws IOException;

    void open(boolean autoCreate) throws IOException;

    boolean isAllowed(String right);

    void setUserCredentials(String user, String credentials) throws IOException;

    void setUserCredentials(String login, String password, String oldPassword) throws IOException;

    void removeRepositoryListener(NutsRepositoryListener listener);

    void addRepositoryListener(NutsRepositoryListener listener);

    NutsRepositoryListener[] getRepositoryListeners();

}

/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by vpc on 1/5/17.
 */
public interface NutsRepository {

    int SPEED_FASTEST = 100;
    int SPEED_FAST = 1000;
    int SPEED_SLOW = 10000;
    int SPEED_SLOWEST = 100000;

    String getRepositoryId();

    NutsWorkspace getWorkspace();

    NutsRepositoryConfigManager getConfigManager();

    NutsRepositorySecurityManager getSecurityManager();

    /**
     * @param id descriptor Id, mandatory as descriptor may not being effective
     * (id is variable or inherited)
     * @param descriptor
     * @param file
     * @return
     */
    NutsId deploy(NutsId id, NutsDescriptor descriptor, File file, NutsSession context);

    void push(NutsId id, String repoId, NutsSession session);

    NutsFile fetch(NutsId id, NutsSession session);

    NutsDescriptor fetchDescriptor(NutsId id, NutsSession session);

    File copyTo(NutsId id, NutsSession session, File localPath);

    File copyDescriptorTo(NutsId id, NutsSession session, File localPath);

    String fetchHash(NutsId id, NutsSession session);

    String fetchDescriptorHash(NutsId id, NutsSession session);

    NutsId resolveId(NutsId id, NutsSession session);

    Iterator<NutsId> find(NutsIdFilter filter, NutsSession session);

    Iterator<NutsId> findVersions(NutsId id, NutsIdFilter idFilter, NutsSession session);

    int getSupportLevel(NutsId id, NutsSession session);

    boolean isSupportedMirroring();

    NutsRepository[] getMirrors();

    NutsRepository getMirror(String repositoryIdPath);

    NutsRepository addMirror(String repositoryId, String location, String type, boolean autoCreate);

    void removeMirror(String repositoryId);

    void save();

    void open(boolean autoCreate);

    void removeRepositoryListener(NutsRepositoryListener listener);

    void addRepositoryListener(NutsRepositoryListener listener);

    NutsRepositoryListener[] getRepositoryListeners();

    void setEnabled(boolean enabled);

    boolean isEnabled();

}

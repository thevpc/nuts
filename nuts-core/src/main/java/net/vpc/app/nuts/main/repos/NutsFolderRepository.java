/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.main.repos;

import net.vpc.app.nuts.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by vpc on 1/5/17.
 */
public class NutsFolderRepository extends NutsCachedRepository {

    public final NutsLogger LOG;

    public NutsFolderRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options, workspace, parentRepository, SPEED_FASTER, true, NutsConstants.RepoTypes.NUTS);
        LOG=workspace.log().of(NutsFolderRepository.class);
        extensions.put("src", "-src.zip");
    }

    @Override
    protected boolean isAllowedOverrideNut(NutsId id) {
        return true;
    }

    @Override
    public NutsDescriptor fetchDescriptorImpl2(NutsId id, NutsRepositorySession session) {
        if (session.getFetchMode() == NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(getWorkspace(), id,new NutsFetchModeNotSupportedException(getWorkspace(),this,session.getFetchMode(),id.toString(),null));
        }
        NutsId id2 = id.builder().faceDescriptor().build();
        throw new NutsNotFoundException(getWorkspace(), id,new IOException("File Not Found : "+lib.getGoodPath(id2)));
    }

    @Override
    public NutsContent fetchContentImpl2(NutsId id, NutsDescriptor descriptor, Path localPath, NutsRepositorySession session) {
        if (session.getFetchMode() == NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(getWorkspace(), id,new NutsFetchModeNotSupportedException(getWorkspace(),this,session.getFetchMode(),id.toString(),null));
        }
        NutsId id2 = id.builder().faceContent().build();
        throw new NutsNotFoundException(getWorkspace(), id,new IOException("File Not Found : "+lib.getGoodPath(id2)));
    }

    @Override
    public Iterator<NutsId> searchImpl2(NutsIdFilter filter, String[] roots, NutsRepositorySession session) {
        return null;
    }

    @Override
    public NutsId searchLatestVersion2(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        return null;
    }

    @Override
    public void updateStatistics2() {
    }

    @Override
    public Iterator<NutsId> searchVersionsImpl2(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {
        return null;
    }

}

/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by vpc on 1/5/17.
 */
public class NutsFolderRepository extends NutsCachedRepository {

//    public final NutsLogger LOG;

    public NutsFolderRepository(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parentRepository) {
        super(options, session, parentRepository, SPEED_FASTER, true, NutsConstants.RepoTypes.NUTS);
//        LOG = session.log().of(NutsFolderRepository.class);
        extensions.put("src", "-src.zip");
    }

    @Override
    protected boolean isSupportedDeployImpl() {
        return true;
    }
    @Override
    protected boolean isAvailableImpl() {
        try {
            if(Files.isDirectory(lib.getStoreLocation())){
                return true;
            }
        } catch (Exception e) {
            //
        }
        try {
            if(Files.isDirectory(cache.getStoreLocation())){
                return true;
            }
        } catch (Exception e) {
            //
        }
        return false;
    }

    @Override
    protected boolean isAllowedOverrideNut(NutsId id) {
        return true;
    }

    @Override
    public NutsDescriptor fetchDescriptorCore(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode == NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        NutsId id2 = id.builder().setFaceDescriptor().build();
        throw new NutsNotFoundException(session, id, new IOException("artifact descriptor not found : " + lib.getGoodPath(id2, session)));
    }

    @Override
    public NutsContent fetchContentCore(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode == NutsFetchMode.REMOTE) {
            throw new NutsNotFoundException(session, id, new NutsFetchModeNotSupportedException(session, this, fetchMode, id.toString(), null));
        }
        NutsId id2 = id.builder().setFaceContent().build();
        throw new NutsNotFoundException(session, id, new IOException("file not found : " + lib.getGoodPath(id2, session)));
    }

    @Override
    public Iterator<NutsId> searchCore(NutsIdFilter filter, String[] roots, NutsFetchMode fetchMode, NutsSession session) {
        return null;
    }

    @Override
    public NutsId searchLatestVersionCore(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
        return null;
    }

    @Override
    public void updateStatistics2(NutsSession session) {
    }

    @Override
    public Iterator<NutsId> searchVersionsCore(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session) {
        return null;
    }

    @Override
    public boolean isAcceptFetchMode(NutsFetchMode mode, NutsSession session) {
        return mode == NutsFetchMode.LOCAL;
    }
    @Override
    public boolean isRemote() {
        return false;
    }

}

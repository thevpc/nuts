/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.maven;

import net.thevpc.nuts.artifact.NArtifactNotFoundException;
import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NFetchMode;
import net.thevpc.nuts.command.NFetchModeNotSupportedException;
import net.thevpc.nuts.core.NAddRepositoryOptions;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.folder.NFolderRepositoryBase;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.solrsearch.MavenSolrSearchCommand;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MvnClient;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.util.NUnsupportedArgumentException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenSolrRepository extends MavenFolderRepository {
    public MavenSolrRepository(NAddRepositoryOptions options, NRepository parentRepository) {
        super(options, parentRepository);
    }

    protected NLog _LOG(){
        return NLog.of(MavenSolrRepository.class);
    }

    @Override
    public NIterator<NId> searchCore(final NDefinitionFilter filter, NPath[] basePaths, NId[] baseIds, NFetchMode fetchMode) {
        if (!acceptedFetchNoCache(fetchMode)) {
            return null;
        }
        if(fetchMode==NFetchMode.REMOTE) {
            MavenSolrSearchCommand cmd = new MavenSolrSearchCommand(this);
            NIterator<NId> aa = cmd.search(filter, baseIds, fetchMode);
            if (aa != null) {
                return aa;
            }
        }
        return super.searchCore(filter, basePaths, baseIds, fetchMode);
    }




    public NIterator<NId> findNonSingleVersionImpl(final NId id, NDefinitionFilter idFilter, NFetchMode fetchMode) {
        if(fetchMode==NFetchMode.REMOTE){
            MavenSolrSearchCommand cmd = new MavenSolrSearchCommand(this);
            NIterator<NId> aa = cmd.search(idFilter, new NId[]{id}, fetchMode);
            if (aa != null) {
                return aa;
            }
        }
        return super.findNonSingleVersionImpl(id, idFilter, fetchMode);
    }
}

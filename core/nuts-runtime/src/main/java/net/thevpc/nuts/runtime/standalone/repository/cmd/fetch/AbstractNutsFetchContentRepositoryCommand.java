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
package net.thevpc.nuts.runtime.standalone.repository.cmd.fetch;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NutsRepositoryCommandBase;
import net.thevpc.nuts.spi.NutsFetchContentRepositoryCommand;

/**
 *
 * @author thevpc
 * %category SPI Base
 */
public abstract class AbstractNutsFetchContentRepositoryCommand extends NutsRepositoryCommandBase<NutsFetchContentRepositoryCommand> implements NutsFetchContentRepositoryCommand {

    protected NutsId id;
    protected NutsPath result;
    protected NutsDescriptor descriptor;
    protected String localPath;

    public AbstractNutsFetchContentRepositoryCommand(NutsRepository repo) {
        super(repo, "fetch");
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        if (super.configureFirst(cmd)) {
            return true;
        }
        return false;
    }

    @Override
    public NutsDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public NutsFetchContentRepositoryCommand setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    @Override
    public String getLocalPath() {
        return localPath;
    }

    @Override
    public NutsFetchContentRepositoryCommand setLocalPath(String localPath) {
        this.localPath = localPath;
        return this;
    }

    @Override
    public NutsPath getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

    @Override
    public NutsFetchContentRepositoryCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsId getId() {
        return id;
    }

}

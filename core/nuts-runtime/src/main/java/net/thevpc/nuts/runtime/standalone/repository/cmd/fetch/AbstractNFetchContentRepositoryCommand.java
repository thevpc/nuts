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
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositoryCommandBase;
import net.thevpc.nuts.spi.NFetchContentRepositoryCommand;

/**
 *
 * @author thevpc
 * %category SPI Base
 */
public abstract class AbstractNFetchContentRepositoryCommand extends NRepositoryCommandBase<NFetchContentRepositoryCommand> implements NFetchContentRepositoryCommand {

    protected NId id;
    protected NPath result;
    protected NDescriptor descriptor;

    public AbstractNFetchContentRepositoryCommand(NRepository repo) {
        super(repo, "fetch");
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        if (super.configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

    @Override
    public NDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public NFetchContentRepositoryCommand setDescriptor(NDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    @Override
    public NPath getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

    @Override
    public NFetchContentRepositoryCommand setId(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public NId getId() {
        return id;
    }

}

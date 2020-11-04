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
package net.thevpc.nuts.runtime.repocommands;

import net.thevpc.nuts.*;

import java.nio.file.Path;

/**
 *
 * @author vpc
 * @category SPI Base
 */
public abstract class AbstractNutsFetchContentRepositoryCommand extends NutsRepositoryCommandBase<NutsFetchContentRepositoryCommand> implements NutsFetchContentRepositoryCommand {

    protected NutsId id;
    protected NutsContent result;
    protected NutsDescriptor descriptor;
    protected Path localPath;

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
    public Path getLocalPath() {
        return localPath;
    }

    @Override
    public NutsFetchContentRepositoryCommand setLocalPath(Path localPath) {
        this.localPath = localPath;
        return this;
    }

    @Override
    public NutsContent getResult() {
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

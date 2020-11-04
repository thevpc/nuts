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
package net.thevpc.nuts.runtime.main.executors;

import net.thevpc.nuts.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class CustomNutsExecutorComponent implements NutsExecutorComponent {

    public NutsId id;

    public CustomNutsExecutorComponent(NutsId id) {
        this.id = id;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsDefinition> nutsDefinition) {
        return NO_SUPPORT;
    }

    @Override
    public void exec(NutsExecutionContext executionContext) {
        List<String> args = new ArrayList<>();
        args.add(id.toString());
        args.addAll(Arrays.asList(executionContext.getArguments()));
        executionContext.getWorkspace()
                .exec()
                .addCommand(args)
                .setSession(executionContext.getExecSession())
                .setEnv(executionContext.getEnv())
                .setDirectory(executionContext.getCwd())
                .setFailFast(true)
                .run();
    }

    @Override
    public void dryExec(NutsExecutionContext executionContext) {
        List<String> args = new ArrayList<>();
        args.add(id.toString());
        args.addAll(Arrays.asList(executionContext.getArguments()));
        executionContext.getWorkspace()
                .exec()
                .addCommand(args)
                .setSession(executionContext.getExecSession())
                .setEnv(executionContext.getEnv())
                .setDirectory(executionContext.getCwd())
                .setFailFast(true)
                .setDry(true)
                .run();
    }

}

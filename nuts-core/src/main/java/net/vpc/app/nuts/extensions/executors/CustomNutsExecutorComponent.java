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
package net.vpc.app.nuts.extensions.executors;

import net.vpc.app.nuts.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class CustomNutsExecutorComponent implements NutsExecutorComponent {

    public static final Logger log = Logger.getLogger(CustomNutsExecutorComponent.class.getName());
    public NutsId id;

    public CustomNutsExecutorComponent(NutsId id) {
        this.id = id;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public int getSupportLevel(NutsFile nutsFile) {
        return NO_SUPPORT;
    }

    public int exec(NutsExecutionContext executionContext) {
        List<String> args = new ArrayList<>();
        args.add(id.toString());
        args.addAll(Arrays.asList(executionContext.getArgs()));
        return executionContext.getWorkspace().exec(
                args.toArray(new String[args.size()]),
                executionContext.getEnv(),
                executionContext.getCwd(),
                executionContext.getSession()
        );
    }

}

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

import java.util.Collection;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsUninstallCommand extends NutsWorkspaceCommand{

    NutsUninstallCommand id(NutsId id);

    NutsUninstallCommand addId(NutsId id);

    NutsUninstallCommand removeId(NutsId id);

    NutsUninstallCommand id(String id);

    NutsUninstallCommand addId(String id);

    NutsUninstallCommand removeId(String id);

    NutsUninstallCommand addIds(NutsId... ids);

    NutsUninstallCommand addIds(String... ids);

    NutsUninstallCommand clearIds();

    NutsId[] getIds();

    NutsUninstallCommand arg(String arg);

    NutsUninstallCommand addArg(String arg);

    NutsUninstallCommand args(Collection<String> args);

    NutsUninstallCommand addArgs(Collection<String> args);

    NutsUninstallCommand args(String... args);

    NutsUninstallCommand addArgs(String... args);

    NutsUninstallCommand clearArgs();

    String[] getArgs();

    NutsUninstallCommand ask();

    NutsUninstallCommand ask(boolean ask);

    NutsUninstallCommand setAsk(boolean ask);

    boolean isAsk();

    NutsUninstallCommand force();

    NutsUninstallCommand force(boolean forceInstall);

    NutsUninstallCommand setForce(boolean forceInstall);

    boolean isForce();

    NutsUninstallCommand trace();

    NutsUninstallCommand trace(boolean trace);

    NutsUninstallCommand setTrace(boolean trace);

    boolean isTrace();

    NutsUninstallCommand erase();

    NutsUninstallCommand erase(boolean erase);

    NutsUninstallCommand setErase(boolean erase);

    boolean isErase();

    NutsUninstallCommand session(NutsSession session);

    NutsUninstallCommand setSession(NutsSession session);

    NutsSession getSession();

    NutsUninstallCommand parseOptions(String... args);

    NutsUninstallCommand run();

    NutsOutputFormat getOutputFormat();

    NutsUninstallCommand props();

    NutsUninstallCommand plain();

    NutsUninstallCommand json();

    NutsUninstallCommand setOutputFormat(NutsOutputFormat outputFormat);

    NutsUninstallCommand outputFormat(NutsOutputFormat outputFormat);

}

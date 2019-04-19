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

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsUndeployCommand {

    NutsUndeployCommand repository(String repository);

    NutsUndeployCommand setRepository(String repository);

    NutsUndeployCommand session(NutsSession session);

    NutsUndeployCommand setSession(NutsSession session);

    String getRepository();

    boolean isForce();

    boolean isTrace();

    boolean isTransitive();

    NutsUndeployCommand force();

    NutsUndeployCommand force(boolean force);

    NutsUndeployCommand setForce(boolean force);

    NutsUndeployCommand trace();

    NutsUndeployCommand trace(boolean trace);

    NutsUndeployCommand setTrace(boolean trace);

    NutsUndeployCommand transitive();

    NutsUndeployCommand transitive(boolean transitive);

    NutsUndeployCommand setTransitive(boolean transitive);

    NutsUndeployCommand formatType(NutsResultFormatType formatType);

    NutsUndeployCommand setFormatType(NutsResultFormatType formatType);

    NutsUndeployCommand json();

    NutsUndeployCommand plain();

    NutsUndeployCommand props();

    NutsResultFormatType getFormatType();

    NutsUndeployCommand run();

    NutsSession getSession();

    NutsId getId();

    NutsUndeployCommand id(NutsId id);

    NutsUndeployCommand id(String id);

    NutsUndeployCommand setId(NutsId id);

    NutsUndeployCommand setId(String id);

    NutsUndeployCommand offline(boolean offline);

    NutsUndeployCommand offline();

    NutsUndeployCommand setOffline(boolean offline);

    boolean isOffline();

}

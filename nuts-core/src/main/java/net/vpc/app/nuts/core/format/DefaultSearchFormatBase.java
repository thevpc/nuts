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
 * Copyright (C) 2016-2019 Taha BEN SALAH
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
package net.vpc.app.nuts.core.format;

import java.io.PrintWriter;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsIterableFormat;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.NutsConfigurableHelper;

/**
 *
 * @author vpc
 */
public abstract class DefaultSearchFormatBase implements NutsIterableFormat {

    private final NutsFetchDisplayOptions displayOptions;
    private final NutsSession session;
    private final PrintWriter writer;
    private final NutsOutputFormat format;

    public DefaultSearchFormatBase(NutsSession session, PrintWriter writer, NutsOutputFormat format) {
        this.format = format;
        this.writer = writer;
        this.session = session;
        displayOptions = new NutsFetchDisplayOptions(session.getWorkspace());
    }

    @Override
    public NutsOutputFormat getOutputFormat() {
        return format;
    }

    public NutsFetchDisplayOptions getDisplayOptions() {
        return displayOptions;
    }

    @Override
    public boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        return NutsConfigurableHelper.configure(this, session.getWorkspace(), skipUnsupported, commandLine);
    }

    @Override
    public NutsIterableFormat configure(boolean skipUnsupported, String... args) {
        return NutsConfigurableHelper.configure(this, session.getWorkspace(), skipUnsupported, args, "search-" + getOutputFormat().name().toLowerCase());
    }

    public NutsWorkspace getWorkspace() {
        return session.getWorkspace();
    }

    public NutsSession getSession() {
        return session;
    }

    public PrintWriter getWriter() {
        return writer;
    }

}

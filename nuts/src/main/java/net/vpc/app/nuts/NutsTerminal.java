/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.InputStream;

/**
 * Created by vpc on 2/20/17.
 */
@Prototype
public interface NutsTerminal extends NutsComponent<Object> {

    void install(NutsWorkspace workspace, InputStream in, NutsPrintStream out, NutsPrintStream err);

    String readLine(String prompt);

    String readPassword(String prompt);

    InputStream getIn();

    void setIn(InputStream in);

    NutsFormattedPrintStream getFormattedOut();

    NutsFormattedPrintStream getFormattedErr();

    NutsPrintStream getOut();

    void setOut(NutsPrintStream out);

    NutsPrintStream getErr();

    void setErr(NutsPrintStream out);
}

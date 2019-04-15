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

import java.io.File;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;

/**
 * 
 * @author vpc
 * @since 0.5.4
 */
public interface NutsIdFormat {

    boolean isOmitNamespace();

    NutsIdFormat setOmitNamespace(boolean omitNamespace);

    boolean isOmitGroup();

    NutsIdFormat setOmitGroup(boolean omitGroup);

    boolean isOmitImportedGroup();

    NutsIdFormat setOmitImportedGroup(boolean omitImportedGroup);

    boolean isOmitEnv();

    NutsIdFormat setOmitEnv(boolean omitEnv);

    boolean isOmitFace();

    NutsIdFormat setOmitFace(boolean omitFace);

    boolean isHighlightImportedGroup();

    NutsIdFormat setHighlightImportedGroup(boolean highlightImportedGroup);

    boolean isHighlightScope();

    NutsIdFormat setHighlightScope(boolean highlightScope);

    boolean isHighlightOptional();

    NutsIdFormat setHighlightOptional(boolean highlightOptional);

//    String formatString(NutsId id);

    String toString(NutsId id);

    void format(NutsId id, PrintStream out);

    void format(NutsId id, Writer out);

    void format(NutsId id, Path out);

    void format(NutsId id, File out);

    void format(NutsId id);

    void format(NutsId id, NutsTerminal terminal);
}

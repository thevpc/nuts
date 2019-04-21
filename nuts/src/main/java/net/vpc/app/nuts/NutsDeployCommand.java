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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsDeployCommand extends NutsWorkspaceCommand{

    NutsDeployCommand content(InputStream stream);

    NutsDeployCommand setContent(InputStream stream);

    NutsDeployCommand content(String path);

    NutsDeployCommand setContent(String path);

    NutsDeployCommand content(File file);

    NutsDeployCommand setContent(File file);

    NutsDeployCommand content(Path file);

    NutsDeployCommand setContent(Path file);

    NutsDeployCommand descriptor(InputStream stream);

    NutsDeployCommand setDescriptor(InputStream stream);

    NutsDeployCommand descriptor(Path path);

    NutsDeployCommand setDescriptor(Path path);

    NutsDeployCommand descriptor(String path);

    NutsDeployCommand setDescriptor(String path);

    NutsDeployCommand descriptor(File file);

    NutsDeployCommand setDescriptor(File file);

    NutsDeployCommand descriptor(URL url);

    NutsDeployCommand setDescriptor(URL url);

    NutsDeployCommand sha1(String sha1);

    NutsDeployCommand setSha1(String sha1);

    NutsDeployCommand descSha1(String descSHA1);

    NutsDeployCommand setDescSha1(String descSHA1);

    NutsDeployCommand content(URL url);

    NutsDeployCommand setContent(URL url);

    NutsDeployCommand descriptor(NutsDescriptor descriptor);

    NutsDeployCommand setDescriptor(NutsDescriptor descriptor);

    NutsDeployCommand repository(String repository);

    NutsDeployCommand setRepository(String repository);
    
    NutsDeployCommand setTargetRepository(String repository);

    NutsDeployCommand session(NutsSession session);

    NutsDeployCommand setSession(NutsSession session);

    String getTargetRepository();

    boolean isForce();

    boolean isOffline();

    boolean isTrace();

    boolean isTransitive();

    NutsDeployCommand force();

    NutsDeployCommand force(boolean force);

    NutsDeployCommand setForce(boolean force);

    NutsDeployCommand offline();

    NutsDeployCommand offline(boolean offline);

    NutsDeployCommand setOffline(boolean offline);

    NutsDeployCommand trace();

    NutsDeployCommand trace(boolean trace);

    NutsDeployCommand setTrace(boolean trace);

    NutsDeployCommand transitive();

    NutsDeployCommand transitive(boolean transitive);

    NutsDeployCommand setTransitive(boolean transitive);

    String getSha1();

    NutsSession getSession();

    NutsDeployCommand run();

    NutsDeployCommand outputFormat(NutsOutputFormat outputFormat);

    NutsDeployCommand setOutputFormat(NutsOutputFormat outputFormat);

    NutsDeployCommand json();

    NutsDeployCommand plain();

    NutsDeployCommand props();

    NutsOutputFormat getOutputFormat();

    NutsId[] getResult();

    NutsId[] getIds();

    NutsDeployCommand addId(String id);

    NutsDeployCommand removeId(String id);

    NutsDeployCommand id(String id);

    NutsDeployCommand removeId(NutsId id);

    NutsDeployCommand addId(NutsId id);

    NutsDeployCommand id(NutsId id);

    NutsDeployCommand clearIds();

    NutsDeployCommand addIds(NutsId... value);

    NutsDeployCommand ids(NutsId... values);

    NutsDeployCommand addIds(String... values);

    NutsDeployCommand ids(String... values);

    NutsDeployCommand to(String repository);

    NutsDeployCommand targetRepository(String repository);

    NutsDeployCommand from(String repository);

    NutsDeployCommand sourceRepository(String repository);

    NutsDeployCommand setSourceRepository(String repository);

    NutsDeployCommand parseOptions(String... args);

    NutsTraceFormat getTraceFormat();

    NutsDeployCommand unsetTraceFormat(NutsOutputFormat f);

    NutsDeployCommand traceFormat(NutsTraceFormat traceFormat);

    NutsDeployCommand setTraceFormat(NutsTraceFormat f);

    NutsTraceFormat[] getTraceFormats();
}

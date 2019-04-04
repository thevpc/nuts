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
package net.vpc.app.nuts.core;

import java.io.IOException;
import java.io.UncheckedIOException;
import net.vpc.app.nuts.NutsDeployment;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreSecurityUtils;
import net.vpc.app.nuts.core.util.TypedObject;
import net.vpc.common.io.IOUtils;
import net.vpc.common.io.InputStreamSource;

public class DefaultNutsDeployment implements NutsDeployment {

    private Object content;
    private Object descriptor;
    private String sha1;
    private String descSHA1;
    private String repository;
    private boolean trace = true;
    private boolean force = false;
    private boolean offline = false;
    private boolean transitive = true;
    private NutsWorkspace ws;

    public DefaultNutsDeployment(NutsWorkspace ws) {
        this.ws = ws;
    }

    public DefaultNutsDeployment setContent(Object stream) {
        content = stream;
        return this;
    }

    public DefaultNutsDeployment setContentPath(String path) {
        content = new TypedObject(String.class, path, "path");
        return this;
    }

    public DefaultNutsDeployment setDescriptor(Object stream) {
        descriptor = stream;
        return this;
    }

    public String getSha1() {
        return sha1;
    }

    public DefaultNutsDeployment setSha1(String sha1) {
        this.sha1 = sha1;
        return this;
    }

    public String getDescSHA1() {
        return descSHA1;
    }

    public DefaultNutsDeployment setDescSHA1(String descSHA1) {
        this.descSHA1 = descSHA1;
        return this;
    }

    @Override
    public Object getContent() {
        return content;
    }

    @Override
    public NutsDescriptor getDescriptor() {
        if (descriptor == null) {
            return null;
        }
        NutsDescriptor mdescriptor = null;
        if (NutsDescriptor.class.isInstance(descriptor)) {
            mdescriptor = (NutsDescriptor) descriptor;
            if (getDescSHA1() != null && !ws.io().getSHA1(mdescriptor).equals(getDescSHA1())) {
                throw new NutsIllegalArgumentException("Invalid Content Hash");
            }
            return mdescriptor;
        } else if (IOUtils.isValidInputStreamSource(descriptor.getClass())) {
            try {
                InputStreamSource inputStreamSource;
                inputStreamSource = CoreIOUtils.toInputStreamSource(descriptor);
                if (getDescSHA1() != null && !CoreSecurityUtils.evalSHA1(inputStreamSource.open(), true).equals(getDescSHA1())) {
                    throw new NutsIllegalArgumentException("Invalid Content Hash");
                }
                return ws.parser().parseDescriptor(inputStreamSource.open(), true);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } else {
            throw new NutsException("Unexpected type " + descriptor.getClass().getName());
        }
    }

    @Override
    public String getRepository() {
        return repository;
    }

    public NutsDeployment setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    @Override
    public boolean isTrace() {
        return trace;
    }

    public NutsDeployment setTrace(boolean traceEnabled) {
        this.trace = traceEnabled;
        return this;
    }

    @Override
    public boolean isForce() {
        return force;
    }

    public NutsDeployment setForce(boolean forceInstall) {
        this.force = forceInstall;
        return this;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    public NutsDeployment setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    public NutsDeployment setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

}

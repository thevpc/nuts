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

import net.vpc.app.nuts.NutsConfirmAction;
import net.vpc.app.nuts.NutsDeployment;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.TypedObject;

public class DefaultNutsDeployment implements NutsDeployment {

    private Object content;
    private Object descriptor;
    private String sha1;
    private String descSHA1;
    private String repositoryName;
    private NutsConfirmAction foundAction;
    private NutsWorkspace ws;

    public DefaultNutsDeployment(NutsWorkspace ws) {
        this.ws = ws;
    }

    public DefaultNutsDeployment setFoundAction(NutsConfirmAction force) {
        this.foundAction = force;
        return this;
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

    public NutsConfirmAction getFoundAction() {
        return foundAction;
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

    public Object getContent() {
        return content;
    }

    public Object getDescriptor() {
        return descriptor;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public DefaultNutsDeployment setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
        return this;
    }


    
}

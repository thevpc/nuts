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
import java.util.Objects;

public class NutsDeployment {

    private TypedObject content;
    private TypedObject descriptor;
    private String sha1;
    private String descSHA1;
    private String repositoryId;
    private NutsConfirmAction foundAction;

    public NutsDeployment setFoundAction(NutsConfirmAction force) {
        this.foundAction = force;
        return this;
    }

    public NutsDeployment setContent(InputStream stream) {
        content = new TypedObject(InputStream.class, stream, null);
        return this;
    }

    public NutsDeployment setContentPath(String path) {
        content = new TypedObject(String.class, path, "path");
        return this;
    }

    public NutsDeployment setContent(File file) {
        content = new TypedObject(File.class, file, null);
        return this;
    }

    public NutsDeployment setDescriptor(InputStream stream) {
        descriptor = new TypedObject(InputStream.class, stream, null);
        return this;
    }

    public NutsDeployment setDescriptorPath(String path) {
        descriptor = new TypedObject(InputStream.class, path, "path");
        return this;
    }

    public NutsDeployment setDescriptor(File file) {
        descriptor = new TypedObject(File.class, file, null);
        return this;
    }

    public NutsDeployment setDescriptor(URL url) {
        descriptor = new TypedObject(URL.class, url, null);
        return this;
    }

    public NutsConfirmAction getFoundAction() {
        return foundAction;
    }

    public String getSha1() {
        return sha1;
    }

    public NutsDeployment setSha1(String sha1) {
        this.sha1 = sha1;
        return this;
    }

    public String getDescSHA1() {
        return descSHA1;
    }

    public NutsDeployment setDescSHA1(String descSHA1) {
        this.descSHA1 = descSHA1;
        return this;
    }

    public TypedObject getContent() {
        return content;
    }

    public NutsDeployment setContent(URL url) {
        content = new TypedObject(URL.class, url, null);
        return this;
    }

    public TypedObject getDescriptor() {
        return descriptor;
    }

    public NutsDeployment setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = new TypedObject(NutsDescriptor.class, descriptor, null);
        return this;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public NutsDeployment setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.content);
        hash = 19 * hash + Objects.hashCode(this.descriptor);
        hash = 19 * hash + Objects.hashCode(this.sha1);
        hash = 19 * hash + Objects.hashCode(this.descSHA1);
        hash = 19 * hash + Objects.hashCode(this.repositoryId);
        hash = 19 * hash + Objects.hashCode(this.foundAction);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutsDeployment other = (NutsDeployment) obj;
        if (this.foundAction != other.foundAction) {
            return false;
        }
        if (!Objects.equals(this.sha1, other.sha1)) {
            return false;
        }
        if (!Objects.equals(this.descSHA1, other.descSHA1)) {
            return false;
        }
        if (!Objects.equals(this.repositoryId, other.repositoryId)) {
            return false;
        }
        if (!Objects.equals(this.content, other.content)) {
            return false;
        }
        if (!Objects.equals(this.descriptor, other.descriptor)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NutsDeployment{" + "content=" + content + ", descriptor=" + descriptor + ", sha1=" + sha1 + ", descSHA1=" + descSHA1 + ", repositoryId=" + repositoryId + ", foundAction=" + foundAction + '}';
    }
    
}

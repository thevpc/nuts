/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author vpc
 */
public class NutsBootOptions implements Serializable {

    private String root;
    private String runtimeId;
    private String runtimeSourceURL;
    private NutsClassLoaderProvider classLoaderProvider;

    public String getRoot() {
        return root;
    }

    public NutsBootOptions setRoot(String workspaceRoot) {
        this.root = workspaceRoot;
        return this;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public NutsBootOptions setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    public String getRuntimeSourceURL() {
        return runtimeSourceURL;
    }

    public NutsBootOptions setRuntimeSourceURL(String runtimeSourceURL) {
        this.runtimeSourceURL = runtimeSourceURL;
        return this;
    }

    public NutsClassLoaderProvider getClassLoaderProvider() {
        return classLoaderProvider;
    }

    public NutsBootOptions setClassLoaderProvider(NutsClassLoaderProvider provider) {
        this.classLoaderProvider = provider;
        return this;
    }

    @Override
    public String toString() {
        return "NutsBootOptions{" + "root=" + root + ", runtimeId=" + runtimeId + ", runtimeSourceURL=" + runtimeSourceURL + ", classLoaderProvider=" + classLoaderProvider + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.root);
        hash = 41 * hash + Objects.hashCode(this.runtimeId);
        hash = 41 * hash + Objects.hashCode(this.runtimeSourceURL);
        hash = 41 * hash + Objects.hashCode(this.classLoaderProvider);
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
        final NutsBootOptions other = (NutsBootOptions) obj;
        if (!Objects.equals(this.root, other.root)) {
            return false;
        }
        if (!Objects.equals(this.runtimeId, other.runtimeId)) {
            return false;
        }
        if (!Objects.equals(this.runtimeSourceURL, other.runtimeSourceURL)) {
            return false;
        }
        if (!Objects.equals(this.classLoaderProvider, other.classLoaderProvider)) {
            return false;
        }
        return true;
    }

    
}

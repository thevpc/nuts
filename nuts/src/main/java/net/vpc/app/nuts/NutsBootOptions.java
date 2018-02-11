/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.io.Serializable;

/**
 *
 * @author vpc
 */
public class NutsBootOptions implements Serializable {

    private String root;
    private String runtimeId;
    private String workspaceRuntimeVersion;
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

}

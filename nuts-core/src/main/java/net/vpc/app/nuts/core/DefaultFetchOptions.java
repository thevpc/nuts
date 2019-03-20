/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

/**
 *
 * @author vpc
 */
public class DefaultFetchOptions {
    private String copyTo;
    private boolean content;
    private boolean effectiveDesc;
    private boolean installInfo;
    private boolean ignoreCache;
    private boolean preferInstalled;
    private boolean installedOnly;

    public String getCopyTo() {
        return copyTo;
    }

    public DefaultFetchOptions setCopyTo(String copyTo) {
        this.copyTo = copyTo;
        return this;
    }

    public boolean isContent() {
        return content;
    }

    public DefaultFetchOptions setContent(boolean content) {
        this.content = content;
        return this;
    }

    public boolean isEffectiveDesc() {
        return effectiveDesc;
    }

    public DefaultFetchOptions setEffectiveDesc(boolean effectiveDesc) {
        this.effectiveDesc = effectiveDesc;
        return this;
    }

    public boolean isInstallInfo() {
        return installInfo;
    }

    public DefaultFetchOptions setInstallInfo(boolean installInfo) {
        this.installInfo = installInfo;
        return this;
    }

    public boolean isIgnoreCache() {
        return ignoreCache;
    }

    public DefaultFetchOptions setIgnoreCache(boolean ignoreCache) {
        this.ignoreCache = ignoreCache;
        return this;
    }

    public boolean isPreferInstalled() {
        return preferInstalled;
    }

    public DefaultFetchOptions setPreferInstalled(boolean preferInstalled) {
        this.preferInstalled = preferInstalled;
        return this;
    }

    public boolean isInstalledOnly() {
        return installedOnly;
    }

    public DefaultFetchOptions setInstalledOnly(boolean installedOnly) {
        this.installedOnly = installedOnly;
        return this;
    }
    
}

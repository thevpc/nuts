/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

/**
 *
 * @author vpc
 */
public class NutsCreateRepositoryOptions {

    private String name;
    private String location;
    private boolean enabled;
    private boolean temporay;
    private boolean failSafe;
    private boolean create;
    private boolean proxy;
    private NutsRepositoryConfig config;

    public NutsCreateRepositoryOptions() {
        this.enabled = true;
    }

    public NutsCreateRepositoryOptions(NutsCreateRepositoryOptions o) {
        this.name = o.name;
        this.location = o.location;
        this.enabled = o.enabled;
        this.temporay = o.temporay;
        this.failSafe = o.failSafe;
        this.create = o.create;
        this.config = o.config;
        this.proxy = o.proxy;
    }

    public String getName() {
        return name;
    }

    public NutsCreateRepositoryOptions setName(String name) {
        this.name = name;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public NutsCreateRepositoryOptions setLocation(String location) {
        this.location = location;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public NutsCreateRepositoryOptions setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isTemporay() {
        return temporay;
    }

    public NutsCreateRepositoryOptions setTemporay(boolean temporay) {
        this.temporay = temporay;
        return this;
    }

    public boolean isFailSafe() {
        return failSafe;
    }

    public NutsCreateRepositoryOptions setFailSafe(boolean failSafe) {
        this.failSafe = failSafe;
        return this;
    }

    public boolean isCreate() {
        return create;
    }

    public NutsCreateRepositoryOptions setCreate(boolean create) {
        this.create = create;
        return this;
    }

    public NutsRepositoryConfig getConfig() {
        return config;
    }

    public NutsCreateRepositoryOptions setConfig(NutsRepositoryConfig config) {
        this.config = config;
        return this;
    }

    public NutsCreateRepositoryOptions copy() {
        return new NutsCreateRepositoryOptions(this);
    }

    public boolean isProxy() {
        return proxy;
    }

    public NutsCreateRepositoryOptions setProxy(boolean proxy) {
        this.proxy = proxy;
        return this;
    }
    
}

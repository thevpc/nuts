package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

public class DefaultNutsDeploymentBuilder implements NutsDeploymentBuilder {

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

    public DefaultNutsDeploymentBuilder(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsDeploymentBuilder setContent(InputStream stream) {
        content = stream;
        return this;
    }

    @Override
    public NutsDeploymentBuilder setContent(String path) {
        content = path;
        return this;
    }

    @Override
    public NutsDeploymentBuilder setContent(File file) {
        content = file;
        return this;
    }

    @Override
    public NutsDeploymentBuilder setContent(Path file) {
        content = file;
        return this;
    }

    @Override
    public NutsDeploymentBuilder setDescriptor(InputStream stream) {
        descriptor = stream;
        return this;
    }

    @Override
    public NutsDeploymentBuilder setDescriptorPath(String path) {
        descriptor = path;
        return this;
    }

    @Override
    public NutsDeploymentBuilder setDescriptor(File file) {
        descriptor = file;
        return this;
    }

    @Override
    public NutsDeploymentBuilder setDescriptor(URL url) {
        descriptor = url;
        return this;
    }

    public String getSha1() {
        return sha1;
    }

    @Override
    public NutsDeploymentBuilder setSha1(String sha1) {
        this.sha1 = sha1;
        return this;
    }

    public String getDescSHA1() {
        return descSHA1;
    }

    @Override
    public NutsDeploymentBuilder setDescSHA1(String descSHA1) {
        this.descSHA1 = descSHA1;
        return this;
    }

    public Object getContent() {
        return content;
    }

    @Override
    public NutsDeploymentBuilder setContent(URL url) {
        content = url;
        return this;
    }

    public Object getDescriptor() {
        return descriptor;
    }

    @Override
    public NutsDeploymentBuilder setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NutsDeploymentBuilder setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    @Override
    public boolean isTrace() {
        return trace;
    }

    @Override
    public NutsDeploymentBuilder setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public boolean isForce() {
        return force;
    }

    @Override
    public NutsDeploymentBuilder setForce(boolean force) {
        this.force = force;
        return this;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NutsDeploymentBuilder setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NutsDeploymentBuilder setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    public NutsWorkspace getWs() {
        return ws;
    }

    public void setWs(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsDeployment build() {
        DefaultNutsDeployment e = new DefaultNutsDeployment(ws);
        e.setContent(content);
        e.setDescriptor(descriptor);
        e.setDescSHA1(descSHA1);
        e.setRepository(repository);
        e.setSha1(sha1);
        e.setTrace(trace);
        e.setForce(force);
        e.setOffline(offline);
        e.setTransitive(transitive);
        return e;
    }

}

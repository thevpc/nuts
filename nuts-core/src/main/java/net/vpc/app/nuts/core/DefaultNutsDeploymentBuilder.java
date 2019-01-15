package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public class DefaultNutsDeploymentBuilder implements NutsDeploymentBuilder {
    private Object content;
    private Object descriptor;
    private String sha1;
    private String descSHA1;
    private String repositoryId;
    private NutsConfirmAction foundAction;
    private NutsWorkspace ws;

    public DefaultNutsDeploymentBuilder(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsDeploymentBuilder setFoundAction(NutsConfirmAction force) {
        this.foundAction = force;
        return this;
    }

    @Override
    public NutsDeploymentBuilder setContent(InputStream stream) {
        content = stream;
        return this;
    }

    @Override
    public NutsDeploymentBuilder setContentPath(String path) {
        content = path;
        return this;
    }

    @Override
    public NutsDeploymentBuilder setContent(File file) {
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

    public NutsConfirmAction getFoundAction() {
        return foundAction;
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

    public String getRepositoryId() {
        return repositoryId;
    }

    @Override
    public NutsDeploymentBuilder setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
        return this;
    }

    @Override
    public NutsDeployment build(){
        DefaultNutsDeployment e = new DefaultNutsDeployment(ws);
        e.setContent(content);
        e.setDescriptor(descriptor);
        e.setDescSHA1(descSHA1);
        e.setFoundAction(foundAction);
        e.setRepositoryId(repositoryId);
        e.setSha1(sha1);
        return e;
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
        final DefaultNutsDeploymentBuilder other = (DefaultNutsDeploymentBuilder) obj;
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

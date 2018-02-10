package net.vpc.app.nuts;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public class NutsDeployment {

    private TypedObject content;
    private TypedObject descriptor;
    private String sha1;
    private String descSHA1;
    private String repositoryId;

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
}

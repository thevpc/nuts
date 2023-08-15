package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

public class NPomLicense {

    private String name;
    private String url;
    private String distribution;
    private String comments;

    public NPomLicense(String name, String url, String distribution, String comments) {
        this.name = name;
        this.url = url;
        this.distribution = distribution;
        this.comments = comments;
    }

    public NPomLicense() {
    }

    public String getName() {
        return name;
    }

    public NPomLicense setName(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public NPomLicense setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getDistribution() {
        return distribution;
    }

    public NPomLicense setDistribution(String distribution) {
        this.distribution = distribution;
        return this;
    }

    public String getComments() {
        return comments;
    }

    public NPomLicense setComments(String comments) {
        this.comments = comments;
        return this;
    }
}

package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.*;

public class NPomContributor {

    private String name;
    private String email;
    private String organization;
    private String organizationUrl;
    private String url;
    private String timeZone;
    private List<String> roles=new ArrayList<>();
    private Map<String,String> properties=new LinkedHashMap<>();
    public NPomContributor() {
    }

    public String getName() {
        return name;
    }

    public NPomContributor setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public NPomContributor setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getOrganization() {
        return organization;
    }

    public NPomContributor setOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    public String getOrganizationUrl() {
        return organizationUrl;
    }

    public NPomContributor setOrganizationUrl(String organizationUrl) {
        this.organizationUrl = organizationUrl;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public NPomContributor setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public NPomContributor setTimeZone(String timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    public List<String> getRoles() {
        return roles;
    }

    public NPomContributor setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public NPomContributor setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }
}

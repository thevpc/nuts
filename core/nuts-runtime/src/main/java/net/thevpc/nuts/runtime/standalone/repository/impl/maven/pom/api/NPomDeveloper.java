    package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NPomDeveloper {

    private String name;
    private String email;
    private String organization;
    private String organizationUrl;
    private String url;
    private String timeZone;
    private List<String> roles=new ArrayList<>();
    private Map<String,String> properties=new LinkedHashMap<>();
    public NPomDeveloper() {
    }

    public String getName() {
        return name;
    }

    public NPomDeveloper setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public NPomDeveloper setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getOrganization() {
        return organization;
    }

    public NPomDeveloper setOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    public String getOrganizationUrl() {
        return organizationUrl;
    }

    public NPomDeveloper setOrganizationUrl(String organizationUrl) {
        this.organizationUrl = organizationUrl;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public NPomDeveloper setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public NPomDeveloper setTimeZone(String timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    public List<String> getRoles() {
        return roles;
    }

    public NPomDeveloper setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public NPomDeveloper setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }
}

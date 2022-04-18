package net.thevpc.nuts;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultNutsDescriptorOrganizationBuilder implements NutsDescriptorOrganizationBuilder {
    private String id;
    private String name;
    private String url;
    private String comments;
    private Map<String, String> properties;

    public DefaultNutsDescriptorOrganizationBuilder() {
    }

    public DefaultNutsDescriptorOrganizationBuilder(NutsDescriptorOrganization other) {
        this(other.getId(), other.getName(), other.getUrl(), other.getComments(), other.getProperties());
    }

    public DefaultNutsDescriptorOrganizationBuilder(String id, String name, String url, String comments, Map<String, String> properties) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.comments = comments;
        this.properties = properties == null ? new LinkedHashMap<>() : new LinkedHashMap<>(properties);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NutsDescriptorOrganizationBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public NutsDescriptorOrganizationBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String getComments() {
        return comments;
    }

    @Override
    public NutsDescriptorOrganizationBuilder setComments(String comments) {
        this.comments = comments;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public NutsDescriptorOrganizationBuilder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public NutsDescriptorOrganizationBuilder setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public NutsDescriptorOrganization readOnly() {
        return new DefaultNutsDescriptorOrganization(this);
    }

    @Override
    public NutsDescriptorOrganizationBuilder builder() {
        return new DefaultNutsDescriptorOrganizationBuilder(this);
    }

    @Override
    public NutsDescriptorOrganization build() {
        return readOnly();
    }

    @Override
    public NutsDescriptorOrganizationBuilder copy() {
        return builder();
    }
}

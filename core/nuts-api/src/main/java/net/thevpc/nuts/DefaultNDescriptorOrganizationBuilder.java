package net.thevpc.nuts;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultNDescriptorOrganizationBuilder implements NDescriptorOrganizationBuilder {
    private String id;
    private String name;
    private String url;
    private String comments;
    private Map<String, String> properties;

    public DefaultNDescriptorOrganizationBuilder() {
    }

    public DefaultNDescriptorOrganizationBuilder(NDescriptorOrganization other) {
        this(other.getId(), other.getName(), other.getUrl(), other.getComments(), other.getProperties());
    }

    public DefaultNDescriptorOrganizationBuilder(String id, String name, String url, String comments, Map<String, String> properties) {
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
    public NDescriptorOrganizationBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public NDescriptorOrganizationBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String getComments() {
        return comments;
    }

    @Override
    public NDescriptorOrganizationBuilder setComments(String comments) {
        this.comments = comments;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public NDescriptorOrganizationBuilder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public NDescriptorOrganizationBuilder setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public NDescriptorOrganization readOnly() {
        return new DefaultNDescriptorOrganization(this);
    }

    @Override
    public NDescriptorOrganizationBuilder builder() {
        return new DefaultNDescriptorOrganizationBuilder(this);
    }

    @Override
    public NDescriptorOrganization build() {
        return readOnly();
    }

    @Override
    public NDescriptorOrganizationBuilder copy() {
        return builder();
    }
}

package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.artifact.NDescriptorOrganization;
import net.thevpc.nuts.artifact.NDescriptorOrganizationBuilder;

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
        this(other.id(), other.name(), other.url(), other.comments(), other.properties());
    }

    public DefaultNDescriptorOrganizationBuilder(String id, String name, String url, String comments, Map<String, String> properties) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.comments = comments;
        this.properties = properties == null ? new LinkedHashMap<>() : new LinkedHashMap<>(properties);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public NDescriptorOrganizationBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public NDescriptorOrganizationBuilder url(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String comments() {
        return comments;
    }

    @Override
    public NDescriptorOrganizationBuilder comments(String comments) {
        this.comments = comments;
        return this;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public NDescriptorOrganizationBuilder id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Map<String, String> properties() {
        return properties;
    }

    @Override
    public NDescriptorOrganizationBuilder properties(Map<String, String> properties) {
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

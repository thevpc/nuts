package net.thevpc.nuts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultNutsDescriptorContributorBuilder implements NutsDescriptorContributorBuilder {
    private String id;
    private String name;
    private String url;
    private String email;
    private List<String> roles;
    private String timezone;
    private List<String> icons;

    private NutsDescriptorOrganization organization;
    private Map<String, String> properties;
    private String comments;

    public DefaultNutsDescriptorContributorBuilder() {
        roles = new ArrayList<>();
        icons = new ArrayList<>();
        properties = new LinkedHashMap<>();
    }

    public DefaultNutsDescriptorContributorBuilder(NutsDescriptorContributor other) {
        this(other.getId(), other.getName(), other.getUrl(), other.getEmail(), other.getRoles(), other.getTimezone(), other.getIcons(), other.getOrganization(), other.getProperties(), other.getComments());
    }

    public DefaultNutsDescriptorContributorBuilder(String id, String name, String url, String email, List<String> roles, String timezone, List<String> icons, NutsDescriptorOrganization organization, Map<String, String> properties, String comments) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.email = email;
        this.roles = roles == null ? new ArrayList<>() : new ArrayList<>(roles);
        this.timezone = timezone;
        this.icons = icons == null ? new ArrayList<>() : new ArrayList<>(icons);
        this.organization = organization;
        this.properties = properties == null ? new LinkedHashMap<>() : new LinkedHashMap<>(properties);
        this.comments = comments;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public NutsDescriptorContributorBuilder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NutsDescriptorContributorBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public NutsDescriptorContributorBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public NutsDescriptorContributorBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public List<String> getRoles() {
        return roles;
    }

    @Override
    public NutsDescriptorContributorBuilder setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    @Override
    public String getTimezone() {
        return timezone;
    }

    @Override
    public NutsDescriptorContributorBuilder setTimezone(String timezone) {
        this.timezone = timezone;
        return this;
    }

    @Override
    public List<String> getIcons() {
        return icons;
    }

    @Override
    public NutsDescriptorContributorBuilder setIcons(List<String> icons) {
        this.icons = icons;
        return this;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public NutsDescriptorContributorBuilder setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public String getComments() {
        return comments;
    }

    @Override
    public NutsDescriptorContributorBuilder setComments(String comments) {
        this.comments = comments;
        return this;
    }

    @Override
    public NutsDescriptorOrganization getOrganization() {
        return organization;
    }

    @Override
    public NutsDescriptorContributorBuilder setOrganization(NutsDescriptorOrganization organization) {
        this.organization = organization;
        return this;
    }

    @Override
    public NutsDescriptorContributor readOnly() {
        return new DefaultNutsDescriptorContributor(this);
    }

    @Override
    public NutsDescriptorContributorBuilder builder() {
        return new DefaultNutsDescriptorContributorBuilder(this);
    }

    @Override
    public NutsDescriptorContributor build() {
        return readOnly();
    }

    @Override
    public NutsDescriptorContributorBuilder copy() {
        return builder();
    }
}

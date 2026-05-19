package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.artifact.NDescriptorContributor;
import net.thevpc.nuts.artifact.NDescriptorContributorBuilder;
import net.thevpc.nuts.artifact.NDescriptorOrganization;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultNDescriptorContributorBuilder implements NDescriptorContributorBuilder {
    private String id;
    private String name;
    private String url;
    private String email;
    private List<String> roles;
    private String timezone;
    private List<String> icons;

    private NDescriptorOrganization organization;
    private Map<String, String> properties;
    private String comments;

    public DefaultNDescriptorContributorBuilder() {
        roles = new ArrayList<>();
        icons = new ArrayList<>();
        properties = new LinkedHashMap<>();
    }

    public DefaultNDescriptorContributorBuilder(NDescriptorContributor other) {
        this(other.id(), other.name(), other.url(), other.email(), other.roles(), other.timezone(), other.icons(), other.organization(), other.properties(), other.comments());
    }

    public DefaultNDescriptorContributorBuilder(String id, String name, String url, String email, List<String> roles, String timezone, List<String> icons, NDescriptorOrganization organization, Map<String, String> properties, String comments) {
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
    public String id() {
        return id;
    }

    @Override
    public NDescriptorContributorBuilder id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public NDescriptorContributorBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public NDescriptorContributorBuilder url(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String email() {
        return email;
    }

    @Override
    public NDescriptorContributorBuilder email(String email) {
        this.email = email;
        return this;
    }

    @Override
    public List<String> roles() {
        return roles;
    }

    @Override
    public NDescriptorContributorBuilder roles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    @Override
    public String timezone() {
        return timezone;
    }

    @Override
    public NDescriptorContributorBuilder timezone(String timezone) {
        this.timezone = timezone;
        return this;
    }

    @Override
    public List<String> icons() {
        return icons;
    }

    @Override
    public NDescriptorContributorBuilder icons(List<String> icons) {
        this.icons = icons;
        return this;
    }

    @Override
    public Map<String, String> properties() {
        return properties;
    }

    @Override
    public NDescriptorContributorBuilder properties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public String comments() {
        return comments;
    }

    @Override
    public NDescriptorContributorBuilder comments(String comments) {
        this.comments = comments;
        return this;
    }

    @Override
    public NDescriptorOrganization organization() {
        return organization;
    }

    @Override
    public NDescriptorContributorBuilder organization(NDescriptorOrganization organization) {
        this.organization = organization;
        return this;
    }

    @Override
    public NDescriptorContributor readOnly() {
        return new DefaultNDescriptorContributor(this);
    }

    @Override
    public NDescriptorContributorBuilder builder() {
        return new DefaultNDescriptorContributorBuilder(this);
    }

    @Override
    public NDescriptorContributor build() {
        return readOnly();
    }

    @Override
    public NDescriptorContributorBuilder copy() {
        return builder();
    }
}

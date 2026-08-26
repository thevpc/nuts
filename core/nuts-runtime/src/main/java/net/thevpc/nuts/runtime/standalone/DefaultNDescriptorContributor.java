package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.artifact.NDescriptorContributor;
import net.thevpc.nuts.artifact.NDescriptorContributorBuilder;
import net.thevpc.nuts.artifact.NDescriptorOrganization;

import java.util.*;

public class DefaultNDescriptorContributor implements NDescriptorContributor {
    private final String id;
    private final String name;
    private final String url;
    private final String email;
    private final List<String> roles;
    private final String timezone;
    private final List<String> icons;

    private final NDescriptorOrganization organization;
    private final Map<String, String> properties;
    private final String comments;

    public DefaultNDescriptorContributor(NDescriptorContributor other) {
        this(other.id(), other.name(), other.url(), other.email(), other.roles(), other.timezone(), other.icons(), other.organization(), other.properties(), other.comments());
    }

    public DefaultNDescriptorContributor(String id, String name, String url, String email, List<String> roles, String timezone, List<String> icons, NDescriptorOrganization organization, Map<String, String> properties, String comments) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.email = email;
        this.roles = roles == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(roles));
        this.timezone = timezone;
        this.icons = icons == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(icons));
        this.organization = organization;
        this.properties = properties == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(properties));
        this.comments = comments;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public String email() {
        return email;
    }

    @Override
    public List<String> roles() {
        return roles;
    }

    @Override
    public String timezone() {
        return timezone;
    }

    @Override
    public List<String> icons() {
        return icons;
    }

    @Override
    public Map<String, String> properties() {
        return properties;
    }

    @Override
    public String comments() {
        return comments;
    }


    @Override
    public NDescriptorOrganization organization() {
        return organization;
    }

    public NDescriptorContributor readOnly() {
        return new DefaultNDescriptorContributor(this);
    }

    public NDescriptorContributorBuilder builder() {
        return new DefaultNDescriptorContributorBuilder(this);
    }
}

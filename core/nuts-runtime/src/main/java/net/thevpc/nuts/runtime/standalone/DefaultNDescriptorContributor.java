package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NDescriptorContributor;
import net.thevpc.nuts.NDescriptorContributorBuilder;
import net.thevpc.nuts.NDescriptorOrganization;

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
        this(other.getId(), other.getName(), other.getUrl(), other.getEmail(), other.getRoles(), other.getTimezone(), other.getIcons(), other.getOrganization(), other.getProperties(), other.getComments());
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
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public List<String> getRoles() {
        return roles;
    }

    @Override
    public String getTimezone() {
        return timezone;
    }

    @Override
    public List<String> getIcons() {
        return icons;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String getComments() {
        return comments;
    }


    @Override
    public NDescriptorOrganization getOrganization() {
        return organization;
    }

    public NDescriptorContributor readOnly() {
        return new DefaultNDescriptorContributor(this);
    }

    public NDescriptorContributorBuilder builder() {
        return new DefaultNDescriptorContributorBuilder(this);
    }
}

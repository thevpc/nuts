package net.thevpc.nuts;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultNutsDescriptorOrganization implements NutsDescriptorOrganization {
    private final String id;
    private final String name;
    private final String url;
    private final String comments;
    private final Map<String, String> properties;

    public DefaultNutsDescriptorOrganization(NutsDescriptorOrganization other) {
        this(other.getId(), other.getName(), other.getUrl(), other.getComments(), other.getProperties());
    }

    public DefaultNutsDescriptorOrganization(String id, String name, String url, String comments, Map<String, String> properties) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.comments = comments;
        this.properties = properties == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(properties));
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
    public String getComments() {
        return comments;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    public NutsDescriptorOrganization readOnly(){
        return this;
    }

    public DefaultNutsDescriptorOrganizationBuilder builder(){
        return new DefaultNutsDescriptorOrganizationBuilder(this);
    }
}

package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.artifact.NDescriptorOrganization;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultNDescriptorOrganization implements NDescriptorOrganization {
    private final String id;
    private final String name;
    private final String url;
    private final String comments;
    private final Map<String, String> properties;

    public DefaultNDescriptorOrganization(NDescriptorOrganization other) {
        this(other.id(), other.name(), other.url(), other.comments(), other.properties());
    }

    public DefaultNDescriptorOrganization(String id, String name, String url, String comments, Map<String, String> properties) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.comments = comments;
        this.properties = properties == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(properties));
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
    public String comments() {
        return comments;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Map<String, String> properties() {
        return properties;
    }

    public NDescriptorOrganization readOnly(){
        return this;
    }

    public DefaultNDescriptorOrganizationBuilder builder(){
        return new DefaultNDescriptorOrganizationBuilder(this);
    }
}

package net.thevpc.nuts;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultNutsDescriptorLicenseBuilder implements NutsDescriptorLicenseBuilder {
    private String id;
    private String name;
    private String url;
    private String distribution;
    private String comments;
    private Map<String, String> properties;

    public DefaultNutsDescriptorLicenseBuilder() {
    }

    public DefaultNutsDescriptorLicenseBuilder(NutsDescriptorLicense other) {
        this(other.getId(), other.getName(), other.getUrl(), other.getDistribution(), other.getComments(), other.getProperties());
    }

    public DefaultNutsDescriptorLicenseBuilder(String id, String name, String url, String distribution, String comments, Map<String, String> properties) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.distribution = distribution;
        this.comments = comments;
        this.properties = properties == null ? new LinkedHashMap<>() : new LinkedHashMap<>(properties);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NutsDescriptorLicenseBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public NutsDescriptorLicenseBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String getDistribution() {
        return distribution;
    }

    @Override
    public NutsDescriptorLicenseBuilder setDistribution(String distribution) {
        this.distribution = distribution;
        return this;
    }

    @Override
    public String getComments() {
        return comments;
    }

    @Override
    public NutsDescriptorLicenseBuilder setComments(String comments) {
        this.comments = comments;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public NutsDescriptorLicenseBuilder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public NutsDescriptorLicenseBuilder setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public NutsDescriptorLicense readOnly() {
        return new DefaultNutsDescriptorLicense(this);
    }

    @Override
    public NutsDescriptorLicenseBuilder builder() {
        return new DefaultNutsDescriptorLicenseBuilder(this);
    }

    @Override
    public NutsDescriptorLicenseBuilder copy() {
        return builder();
    }

    @Override
    public NutsDescriptorLicense build() {
        return readOnly();
    }
}

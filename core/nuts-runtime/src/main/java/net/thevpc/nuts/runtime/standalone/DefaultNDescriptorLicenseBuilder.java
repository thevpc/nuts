package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NDescriptorLicense;
import net.thevpc.nuts.NDescriptorLicenseBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultNDescriptorLicenseBuilder implements NDescriptorLicenseBuilder {
    private String id;
    private String name;
    private String url;
    private String distribution;
    private String comments;
    private String date;
    private Map<String, String> properties;

    public DefaultNDescriptorLicenseBuilder() {
    }

    public DefaultNDescriptorLicenseBuilder(NDescriptorLicense other) {
        this(other.getId(), other.getName(), other.getUrl(), other.getDistribution(), other.getComments(), other.getDate(), other.getProperties());
    }

    public DefaultNDescriptorLicenseBuilder(String id, String name, String url, String distribution, String comments, String date, Map<String, String> properties) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.distribution = distribution;
        this.comments = comments;
        this.date = date;
        this.properties = properties == null ? new LinkedHashMap<>() : new LinkedHashMap<>(properties);
    }

    @Override
    public String getDate() {
        return date;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NDescriptorLicenseBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public NDescriptorLicenseBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String getDistribution() {
        return distribution;
    }

    @Override
    public NDescriptorLicenseBuilder setDistribution(String distribution) {
        this.distribution = distribution;
        return this;
    }

    @Override
    public String getComments() {
        return comments;
    }

    @Override
    public NDescriptorLicenseBuilder setComments(String comments) {
        this.comments = comments;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public NDescriptorLicenseBuilder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public NDescriptorLicenseBuilder setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }


    @Override
    public NDescriptorLicense readOnly() {
        return new DefaultNDescriptorLicense(this);
    }

    @Override
    public NDescriptorLicenseBuilder builder() {
        return new DefaultNDescriptorLicenseBuilder(this);
    }

    public DefaultNDescriptorLicenseBuilder setDate(String date) {
        this.date = date;
        return this;
    }

    @Override
    public NDescriptorLicenseBuilder copy() {
        return builder();
    }

    @Override
    public NDescriptorLicense build() {
        return readOnly();
    }
}

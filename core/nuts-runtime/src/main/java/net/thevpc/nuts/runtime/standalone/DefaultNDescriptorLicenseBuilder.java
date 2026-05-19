package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.artifact.NDescriptorLicense;
import net.thevpc.nuts.artifact.NDescriptorLicenseBuilder;

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
        this(other.id(), other.name(), other.url(), other.distribution(), other.comments(), other.date(), other.properties());
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
    public String date() {
        return date;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public NDescriptorLicenseBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public NDescriptorLicenseBuilder url(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String distribution() {
        return distribution;
    }

    @Override
    public NDescriptorLicenseBuilder distribution(String distribution) {
        this.distribution = distribution;
        return this;
    }

    @Override
    public String comments() {
        return comments;
    }

    @Override
    public NDescriptorLicenseBuilder comments(String comments) {
        this.comments = comments;
        return this;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public NDescriptorLicenseBuilder id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Map<String, String> properties() {
        return properties;
    }

    @Override
    public NDescriptorLicenseBuilder properties(Map<String, String> properties) {
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

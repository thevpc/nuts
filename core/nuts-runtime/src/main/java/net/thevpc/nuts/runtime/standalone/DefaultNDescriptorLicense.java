package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.artifact.NDescriptorLicense;
import net.thevpc.nuts.artifact.NDescriptorLicenseBuilder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultNDescriptorLicense implements NDescriptorLicense {
    private final String id;
    private final String name;
    private final String date;
    private final String url;
    private final String distribution;
    private final String comments;
    private final Map<String, String> properties;

    public DefaultNDescriptorLicense(NDescriptorLicense other) {
        this(other.id(), other.name(), other.url(), other.distribution(), other.comments(), other.date(), other.properties());
    }

    public DefaultNDescriptorLicense(String id, String name, String url, String distribution, String comments, String date, Map<String, String> properties) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.url = url;
        this.distribution = distribution;
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
    public String distribution() {
        return distribution;
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
    public String date() {
        return date;
    }

    @Override
    public Map<String, String> properties() {
        return properties;
    }


    public NDescriptorLicense readOnly() {
        return this;
    }

    public NDescriptorLicenseBuilder builder() {
        return new DefaultNDescriptorLicenseBuilder(this);
    }
}

package net.thevpc.nuts;

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
        this(other.getId(), other.getName(), other.getUrl(), other.getDistribution(), other.getComments(), other.getDate(), other.getProperties());
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
    public String getName() {
        return name;
    }


    @Override
    public String getUrl() {
        return url;
    }


    @Override
    public String getDistribution() {
        return distribution;
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
    public String getDate() {
        return date;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }


    public NDescriptorLicense readOnly() {
        return this;
    }

    public NDescriptorLicenseBuilder builder() {
        return new DefaultNDescriptorLicenseBuilder(this);
    }
}

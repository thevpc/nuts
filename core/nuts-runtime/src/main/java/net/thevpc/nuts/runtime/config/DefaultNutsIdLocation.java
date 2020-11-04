package net.thevpc.nuts.runtime.config;

import net.thevpc.nuts.NutsIdLocation;

import java.util.Objects;

public class DefaultNutsIdLocation implements NutsIdLocation {
    private final String url;
    private final String classifier;
    private final String region;

    public DefaultNutsIdLocation(String url, String classifier, String region) {
        this.url = url;
        this.classifier = classifier;
        this.region = region;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public String getRegion() {
        return region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsIdLocation that = (DefaultNutsIdLocation) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(classifier, that.classifier) &&
                Objects.equals(region, that.region)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, classifier, region);
    }
}

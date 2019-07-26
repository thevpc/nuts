package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsIdLocation;
import net.vpc.app.nuts.NutsIdLocationBuilder;

import java.util.Objects;

public class DefaultNutsIdLocation implements NutsIdLocation {
    private final String url;
    private final String classifier;

    public DefaultNutsIdLocation(String url, String classifier) {
        this.url = url;
        this.classifier = classifier;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsIdLocation that = (DefaultNutsIdLocation) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(classifier, that.classifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, classifier);
    }
}

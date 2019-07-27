package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsIdLocation;
import net.vpc.app.nuts.NutsIdLocationBuilder;

import java.util.Objects;

public class DefaultNutsIdLocationBuilder implements NutsIdLocationBuilder {
    private String url;
    private String classifier;

    public DefaultNutsIdLocationBuilder() {
    }

    public DefaultNutsIdLocationBuilder(NutsIdLocation value) {
        setUrl(value.getUrl());
        setClassifier(value.getClassifier());
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public DefaultNutsIdLocationBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public DefaultNutsIdLocationBuilder setClassifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    @Override
    public NutsIdLocationBuilder clear() {
        setUrl(null);
        setClassifier(null);
        return this;
    }

    @Override
    public NutsIdLocationBuilder set(NutsIdLocationBuilder value) {
        if(value==null){
            clear();
        }else{
            setUrl(value.getUrl());
            setClassifier(value.getClassifier());
        }
        return this;
    }

    @Override
    public NutsIdLocationBuilder set(NutsIdLocation value) {
        if(value==null){
            clear();
        }else{
            setUrl(value.getUrl());
            setClassifier(value.getClassifier());
        }
        return this;
    }

    @Override
    public NutsIdLocationBuilder url(String value) {
        return setUrl(value);
    }

    @Override
    public NutsIdLocationBuilder classifier(String value) {
        return setClassifier(value);
    }

    @Override
    public NutsIdLocation build() {
        return new DefaultNutsIdLocation(url, classifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsIdLocationBuilder that = (DefaultNutsIdLocationBuilder) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(classifier, that.classifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, classifier);
    }
}

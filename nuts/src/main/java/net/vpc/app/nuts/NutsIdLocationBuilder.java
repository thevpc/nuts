package net.vpc.app.nuts;

public interface NutsIdLocationBuilder {
    String getUrl();

    NutsIdLocationBuilder url(String value);

    NutsIdLocationBuilder setUrl(String value);

    String getClassifier();

    NutsIdLocationBuilder classifier(String value);

    NutsIdLocationBuilder setClassifier(String value);

    String getRegion();

    NutsIdLocationBuilder region(String value);

    NutsIdLocationBuilder setRegion(String value);

    NutsIdLocationBuilder set(NutsIdLocationBuilder value);

    NutsIdLocationBuilder set(NutsIdLocation value);

    NutsIdLocationBuilder clear();

    NutsIdLocation build();
}

package net.vpc.app.nuts;

public interface NutsIdLocationBuilder {
    String getUrl();

    NutsIdLocationBuilder url(String value);

    NutsIdLocationBuilder setUrl(String value);

    String getClassifier();

    NutsIdLocationBuilder classifier(String value);

    NutsIdLocationBuilder setClassifier(String value);

    NutsIdLocation build();
}

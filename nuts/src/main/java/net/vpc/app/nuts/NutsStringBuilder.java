package net.vpc.app.nuts;

public interface NutsStringBuilder {
    NutsStringBuilder appendFormatted(String s);

    NutsStringBuilder append(Object s);

    NutsStringBuilder appendRaw(String s);

    NutsStringBuilder appendRaw(String type, String s);

    NutsStringBuilder appendHashed(Object o, Object hash);

    NutsStringBuilder appendRandom(Object o);

    NutsStringBuilder appendHashed(Object o);

    String toFormattedString();

    NutsString toNutsString();

    String toFilteredString();
    NutsStringBuilder clear();
}

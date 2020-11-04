package net.thevpc.nuts;

public interface NutsStringBuilder {
    NutsStringBuilder append(String s);

    NutsStringBuilder append(Object s);

    NutsStringBuilder appendRaw(String s);

    NutsStringBuilder appendRaw(String type, String s);

    NutsStringBuilder appendHashed(Object o, Object hash);

    NutsStringBuilder appendHashed(Object o);

    NutsStringBuilder appendRandom(Object o);

    String toFormattedString();

    NutsString toNutsString();

    String toFilteredString();

    NutsStringBuilder clear();
}

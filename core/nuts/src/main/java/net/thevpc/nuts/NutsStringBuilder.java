package net.thevpc.nuts;

public interface NutsStringBuilder extends NutsStringBase{
    NutsStringBuilder append(String s);

    NutsStringBuilder append(Object s);

    NutsStringBuilder appendRaw(String s);

    NutsStringBuilder append(String formatType, String rawString);

    NutsStringBuilder appendHashed(Object o, Object hash);

    NutsStringBuilder appendHashed(Object o);

    NutsStringBuilder appendRandom(Object o);

    String toFormattedString();

    String toFilteredString();

    NutsStringBuilder clear();
}

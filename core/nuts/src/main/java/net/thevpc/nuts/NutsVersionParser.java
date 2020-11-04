package net.thevpc.nuts;

public interface NutsVersionParser {

    /**
     * return version instance representing the {@code version} string
     *
     * @param version string (may be null)
     * @return version instance representing the {@code version} string
     */
    NutsVersion parse(String version);

    NutsVersionParser setLenient(boolean lenient);

    boolean isLenient();


}

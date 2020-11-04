package net.thevpc.nuts;

public interface NutsIdManager {
    NutsIdParser parser();
    NutsIdFormat formatter();

    NutsIdFormat formatter(NutsId id);

    NutsIdFilterManager filter();

    /**
     * create new instance of id builder
     * @return new instance of id builder
     */
    NutsIdBuilder builder();

    /**
     * detect nuts id from resources containing the given class
     * or null if not found. If multiple resolutions return the first.
     * @param clazz to search for
     * @return nuts id detected from resources containing the given class
     */
    NutsId resolveId(Class clazz);

    /**
     * detect all nuts ids from resources containing the given class.
     * @param clazz to search for
     * @return all nuts ids detected from resources containing the given class
     */
    NutsId[] resolveIds(Class clazz);

}

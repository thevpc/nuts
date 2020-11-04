package net.thevpc.nuts;

import java.util.Set;

public interface NutsDependencyManager {

    /**
     * return mutable id builder instance initialized with {@code this} instance.
     *
     * @return mutable id builder instance initialized with {@code this} instance
     */
    NutsDependencyBuilder builder();

    NutsDependencyParser parser();


    /**
     * create dependency format instance
     *
     * @return dependency format
     * @since 0.5.5
     */
    NutsDependencyFormat formatter();

    /**
     * create dependency format instance
     *
     * @param dependency dependency
     * @return dependency format
     * @since 0.5.5
     */
    NutsDependencyFormat formatter(NutsDependency dependency);

    NutsDependencyFilterManager filter();

    Set<NutsDependencyScope> toScopeSet(NutsDependencyScopePattern other);
}

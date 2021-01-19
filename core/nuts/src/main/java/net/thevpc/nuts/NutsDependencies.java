package net.thevpc.nuts;

import java.util.List;
import java.util.stream.Stream;

public interface NutsDependencies extends Iterable<NutsDependency> {
    List<NutsId> ids();

    NutsDependencyFilter filter();

    List<NutsDependency> immediate();

    List<NutsDependency> all();

    Stream<NutsDependency> stream();

    /**
     * return all or some of the transitive dependencies of the current Artifact as Tree result of the search command
     *
     * @return all or some of the transitive dependencies of the current Artifact as Tree result of the search command.
     */
    List<NutsDependencyTreeNode> nodes();
}

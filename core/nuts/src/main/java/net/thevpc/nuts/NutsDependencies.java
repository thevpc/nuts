package net.thevpc.nuts;

import java.util.List;
import java.util.stream.Stream;

public interface NutsDependencies extends Iterable<NutsDependency> {
    List<NutsId> sourceIds();

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

    /**
     * dependencies merged with ids, which may constitute a full classpath
     *
     * @return dependencies merged with ids, which may constitute a full classpath
     */
    List<NutsDependency> mergedDependencies();

    List<NutsDependencyTreeNode> mergedNodes();
}

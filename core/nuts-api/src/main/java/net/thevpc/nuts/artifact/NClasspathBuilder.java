package net.thevpc.nuts.artifact;

import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.pipeline.NStream;

import java.util.List;

/**
 * @since 1.0.0
 */
public interface NClasspathBuilder extends NComponent, Iterable<NClasspathEntry> {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NClasspathBuilder of() {
        return NExtensions.of(NClasspathBuilder.class);
    }

    /**
     * Adds add.
     *
     * @param def def
     * @return add result
     */
    NClasspathBuilder add(NClasspathEntry def);

    /**
     * Adds add.
     *
     * @param def def
     * @return add result
     */
    NClasspathBuilder add(NDefinition def);

    /**
     * Adds add.
     *
     * @param id id
     * @return add result
     */
    NClasspathBuilder add(NId id);          // resolves to NDefinition internally

    /**
     * Adds add.
     *
     * @param path path
     * @return add result
     */
    NClasspathBuilder add(NPath path);    // raw jar/url, no coordinates

    /**
     * Solver.
     *
     * @param solver solver
     * @return solver result
     */
    NClasspathBuilder solver(String solver);   // pass-through to solver

    /**
     * Ignore current environment.
     *
     * @param ignoreCurrentEnvironment ignore current environment
     * @return ignore current environment result
     */
    NClasspathBuilder ignoreCurrentEnvironment(boolean ignoreCurrentEnvironment); // pass-through to solver

    /**
     * Dependency filter.
     *
     * @param filter filter
     * @return dependency filter result
     */
    NClasspathBuilder dependencyFilter(NDependencyFilter filter);   // pass-through to solver

    /**
     * Repository filter.
     *
     * @param filter filter
     * @return repository filter result
     */
    NClasspathBuilder repositoryFilter(NRepositoryFilter filter);   // pass-through to solver

    /**
     * Resolve.
     *
     * @return resolve result
     */
    List<NClasspathEntry> resolve();   // does the actual solve+walk+dedup work

    /**
     * Checks if is empty.
     *
     * @return is empty result
     */
    boolean isEmpty();

    /**
     * Size.
     *
     * @return size result
     */
    int size();

    /**
     * Stream.
     *
     * @return stream result
     */
    NStream<NClasspathEntry> stream();

    /**
     * Converts to list.
     *
     * @return to list result
     */
    List<NClasspathEntry> toList();
}
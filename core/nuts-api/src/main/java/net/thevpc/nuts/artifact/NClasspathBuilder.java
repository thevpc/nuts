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
    static NClasspathBuilder of() {
        return NExtensions.of(NClasspathBuilder.class);
    }

    NClasspathBuilder add(NClasspathEntry def);

    NClasspathBuilder add(NDefinition def);

    NClasspathBuilder add(NId id);          // resolves to NDefinition internally

    NClasspathBuilder add(NPath path);    // raw jar/url, no coordinates

    NClasspathBuilder solver(String solver);   // pass-through to solver

    NClasspathBuilder ignoreCurrentEnvironment(boolean ignoreCurrentEnvironment); // pass-through to solver

    NClasspathBuilder dependencyFilter(NDependencyFilter filter);   // pass-through to solver

    NClasspathBuilder repositoryFilter(NRepositoryFilter filter);   // pass-through to solver

    List<NClasspathEntry> resolve();   // does the actual solve+walk+dedup work

    boolean isEmpty();

    int size();

    NStream<NClasspathEntry> stream();

    List<NClasspathEntry> toList();
}
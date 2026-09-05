package net.thevpc.nuts.runtime.standalone.atrifact;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NFetch;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.pipeline.NStream;

import java.util.*;

@NScore(fixed = NScorable.DEFAULT_SCORE)
@NComponentScope(NScopeType.PROTOTYPE)
public class DefaultNClasspathBuilder implements NClasspathBuilder {
    private final List<NClasspathEntry> items = new ArrayList<>();
    private String solver;
    private NDependencyFilter dependencyFilter;
    private NRepositoryFilter repositoryFilter;
    private boolean ignoreCurrentEnvironment;

    public DefaultNClasspathBuilder() {
    }

    public DefaultNClasspathBuilder add(NClasspathEntry item) {
        this.items.add(item);
        return this;
    }

    @Override
    public NClasspathBuilder solver(String solver) {
        this.solver = solver;
        return this;
    }

    @Override
    public NClasspathBuilder dependencyFilter(NDependencyFilter dependencyFilter) {
        this.dependencyFilter = dependencyFilter;
        return this;
    }

    @Override
    public NClasspathBuilder repositoryFilter(NRepositoryFilter repositoryFilter) {
        this.repositoryFilter = repositoryFilter;
        return this;
    }

    @Override
    public NClasspathBuilder add(NDefinition def) {
        items.add(new DefaultNClasspathEntry(def));
        return this;
    }

    @Override
    public NClasspathBuilder add(NId id) {
        items.add(new DefaultNClasspathEntry(NFetch.of(id)
                .dependencyFilter(dependencyFilter)
                .repositoryFilter(repositoryFilter)
                .getResultDefinition()));
        return this;
    }

    @Override
    public NClasspathBuilder add(NPath path) {
        items.add(new DefaultNClasspathEntry(path));
        return this;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public int size() {
        return items.size();
    }


    @Override
    public Iterator<NClasspathEntry> iterator() {
        return items.iterator();
    }

    @Override
    public NStream<NClasspathEntry> stream() {
        return NStream.ofStream(items.stream());
    }

    @Override
    public List<NClasspathEntry> toList() {
        return new ArrayList<>(items);
    }

    public List<NClasspathEntry> resolve() {
        List<NClasspathEntry> result = new ArrayList<>();
        NDependencySolver solver = _createSolver();
        int baseCount = 0;
        Set<String> baseVisited = new HashSet<>();
        for (NClasspathEntry item : items) {
            switch (item.type()){
                case PATH:{
                    break;
                }
                case  DEPENDENCY:{
                    NId itemId = item.id();
                    if (baseVisited.add(itemId.shortName())) {
                        solver.add(item.dependency());
                        baseCount++;
                    }
                    break;
                }
                case DEFINITION:{
                    NId itemId = item.id();
                    if (baseVisited.add(itemId.shortName())) {
                        solver.add(item.definition());
                        baseCount++;
                    }
                }
            }
        }
        List<NDefinition> newDefs = new ArrayList<>();
        Map<String, NDefinition> byShortName = new LinkedHashMap<>();
        Set<String> visited = new HashSet<>();
        for (NDependency d : solver.solve().transitiveWithSource().toList()) {
            NDefinition dd = NFetch.of(d.toId()).getResultDefinition();
            newDefs.add(dd);
            byShortName.put(dd.id().shortName(), dd);
        }
        for (NClasspathEntry item : items) {
            switch (item.type()){
                case PATH:{
                    result.add(item);
                    break;
                }
                case DEPENDENCY: {
                    if (baseCount == 1) {
                        // no need to resolve, just include all
                        for (NDefinition newDef : newDefs) {
                            result.add(new DefaultNClasspathEntry(newDef));
                            visited.add(newDef.id().shortName());
                        }
                    } else {
                        NDependencySolver solver2 = _createSolver();
                        solver2.add(item.dependency());
                        for (NDependency d : solver2.solve().transitiveWithSource().toList()) {
                            NDefinition exiting = byShortName.get(d.toId().shortName());
                            if (exiting != null && visited.add(d.toId().shortName())) {
                                result.add(new DefaultNClasspathEntry(exiting));
                            }
                        }
                    }
                    break;
                }
                case DEFINITION:{
                    if (baseCount == 1) {
                        // no need to resolve, just include all
                        for (NDefinition newDef : newDefs) {
                            result.add(new DefaultNClasspathEntry(newDef));
                            visited.add(newDef.id().shortName());
                        }
                    } else {
                        NDependencySolver solver2 = _createSolver();
                        solver2.add(item.definition());
                        for (NDependency d : solver2.solve().transitiveWithSource().toList()) {
                            NDefinition exiting = byShortName.get(d.toId().shortName());
                            if (exiting != null && visited.add(d.toId().shortName())) {
                                result.add(new DefaultNClasspathEntry(exiting));
                            }
                        }
                    }
                }

            }
        }
        // Safety net: anything the global solve resolved but no per-item walk ever
        // reached (due to isolated-solve version divergence) still needs to be on
        // the classpath. Append leftovers in global-solve order.
        for (Map.Entry<String, NDefinition> e : byShortName.entrySet()) {
            if (visited.add(e.getKey())) {
                result.add(new DefaultNClasspathEntry(e.getValue()));
            }
        }
        return result;
    }

    public NClasspathBuilder ignoreCurrentEnvironment(boolean ignoreCurrentEnvironment) {
        this.ignoreCurrentEnvironment = ignoreCurrentEnvironment;
        return this;
    }

    private NDependencySolver _createSolver() {
        NDependencyFilter effFilter = dependencyFilter;
        if (effFilter == null) {
            effFilter = NDependencyFilter.ofRunnable();
        }
        return NDependencySolver.of(solver).repositoryFilter(repositoryFilter).dependencyFilter(effFilter).ignoreCurrentEnvironment(ignoreCurrentEnvironment);
    }
}

package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.artifact.NDependencyFilters;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.platform.NExecutionEngineFamily;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class NDependencyPlatformFamilyFilter extends AbstractDependencyFilter  {

    private Set<NExecutionEngineFamily> accepted = EnumSet.noneOf(NExecutionEngineFamily.class);

    public NDependencyPlatformFamilyFilter() {
        super(NFilterOp.CUSTOM);
    }

    private NDependencyPlatformFamilyFilter(Collection<NExecutionEngineFamily> accepted) {
        super(NFilterOp.CUSTOM);
        this.accepted = EnumSet.copyOf(accepted);
    }

    public NDependencyPlatformFamilyFilter(String accepted) {
        super(NFilterOp.CUSTOM);
        this.accepted = EnumSet.noneOf(NExecutionEngineFamily.class);
        for (NId e : NId.getList(accepted).get()) {
            if (!e.isBlank()) {
                this.accepted.add(NExecutionEngineFamily.parse(e.getArtifactId()).orNull());
            }
        }
    }

    public NDependencyPlatformFamilyFilter add(Collection<NExecutionEngineFamily> oses) {
        EnumSet<NExecutionEngineFamily> s2 = EnumSet.copyOf(this.accepted);
        NCollections.addAllNonNull(s2, oses);
        return new NDependencyPlatformFamilyFilter(s2);
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        List<String> current = dependency.getCondition().getPlatform();
        boolean empty = true;
        if (current != null) {
            for (String e : current) {
                if (!e.isEmpty()) {
                    empty = false;
                    if (accepted.contains(NExecutionEngineFamily.parse(e).orNull())) {
                        return true;
                    }
                }
            }
        }
        return empty;
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrEqOrIn("platform",
                        accepted.stream().map(x -> x.id()).collect(Collectors.toList())
                );
    }

    @Override
    public NDependencyFilter simplify() {
        return accepted.isEmpty() ? NDependencyFilters.of().always() : this;
    }
}

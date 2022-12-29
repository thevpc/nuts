package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.xtra.glob.GlobUtils;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class NExclusionDependencyFilter extends AbstractDependencyFilter{

    private final NDependencyFilter base;
    private final NId[] exclusions;

    public NExclusionDependencyFilter(NSession session, NDependencyFilter base, NId[] exclusions) {
        super(session, NFilterOp.CUSTOM);
        this.base = base;
        this.exclusions = exclusions;
    }

    @Override
    public boolean acceptDependency(NId from, NDependency dependency, NSession session) {
        if (base != null) {
            if (!base.acceptDependency(from, dependency, session)) {
                return false;
            }
        }
        for (NId exclusion : exclusions) {
            NId nutsId = dependency.toId();
            if (
                    GlobUtils.ofExact(exclusion.getGroupId()).matcher(NStringUtils.trim(nutsId.getGroupId())).matches()
                    && GlobUtils.ofExact(exclusion.getArtifactId()).matcher(NStringUtils.trim(nutsId.getArtifactId())).matches()
                    && exclusion.getVersion().filter(session).acceptVersion(nutsId.getVersion(), session)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NDependencyFilter simplify() {
        if (exclusions.length == 0) {
            return base;
        }
        NDependencyFilter base2 = CoreFilterUtils.simplify(base);
        if (base2 != base) {
            return new NExclusionDependencyFilter(getSession(),base2, exclusions);
        }
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.base);
        hash = 67 * hash + Arrays.deepHashCode(this.exclusions);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NExclusionDependencyFilter other = (NExclusionDependencyFilter) obj;
        if (!Objects.equals(this.base, other.base)) {
            return false;
        }
        if (!Arrays.deepEquals(this.exclusions, other.exclusions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return base + (exclusions == null ? "" : (" excludes " + Arrays.stream(exclusions)
                .map(x -> x.getLongName())
                .collect(Collectors.joining(","))));
    }

}

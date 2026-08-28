package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.xtra.glob.GlobUtils;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class NExclusionDependencyFilter extends NDependencyFilterBase {

    private final NDependencyFilter base;
    private final NId[] exclusions;

    public NExclusionDependencyFilter(NDependencyFilter base, NId[] exclusions) {
        super(NFilterOp.CUSTOM);
        this.base = base;
        this.exclusions = exclusions;
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        if (base != null) {
            if (!base.acceptDependency(dependency, from)) {
                return false;
            }
        }
        for (NId exclusion : exclusions) {
            NId nutsId = dependency.toId();
            if (
                    GlobUtils.ofExact(exclusion.groupId()).matcher(NStringUtils.strip(nutsId.groupId())).matches()
                    && GlobUtils.ofExact(exclusion.artifactId()).matcher(NStringUtils.strip(nutsId.artifactId())).matches()
                    && exclusion.version().toFilter().acceptVersion(nutsId.version())) {
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
            return new NExclusionDependencyFilter(base2, exclusions);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NExclusionDependencyFilter that = (NExclusionDependencyFilter) o;
        return Objects.equals(base, that.base) && Objects.deepEquals(exclusions, that.exclusions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), base, Arrays.hashCode(exclusions));
    }

    @Override
    public String toString() {
        return base + (exclusions == null ? "" : (" excludes " + Arrays.stream(exclusions)
                .map(x -> x.longName())
                .collect(Collectors.joining(","))));
    }

}

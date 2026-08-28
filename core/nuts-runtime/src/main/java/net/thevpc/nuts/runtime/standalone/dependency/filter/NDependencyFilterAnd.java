package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.runtime.standalone.dependency.util.NComplexExpressionString;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NDependencyFilterAnd extends NDependencyFilterBase implements NComplexExpressionString {

    private final NDependencyFilter[] all;

    public NDependencyFilterAnd(NDependencyFilter... all) {
        super(NFilterOp.AND);
        this.all = all;
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        for (NDependencyFilter nDependencyFilter : all) {
            if (nDependencyFilter != null && !nDependencyFilter.acceptDependency(dependency, from)) {
                return false;
            }
        }
        return true;
    }

    public NDependencyFilter simplify() {
        return CoreFilterUtils.simplifyFilterAnd(NDependencyFilter.class,this,all);
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrAnd(Arrays.stream(all).map(NComplexExpressionString::toString).collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NDependencyFilterAnd that = (NDependencyFilterAnd) o;
        return Objects.deepEquals(all, that.all);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Arrays.hashCode(all));
    }

    public List<NFilter> subFilters() {
        return Arrays.asList(all);
    }
}

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

public class NDependencyFilterOr extends AbstractDependencyFilter implements NComplexExpressionString {

    private final NDependencyFilter[] all;

    public NDependencyFilterOr(NDependencyFilter... all) {
        super(NFilterOp.OR);
        this.all = all;
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        boolean one = false;
        for (NDependencyFilter nDependencyFilter : all) {
            if (nDependencyFilter != null) {
                one = true;
                if (nDependencyFilter.acceptDependency(dependency, from)) {
                    return true;
                }
            }
        }
        return one ? false : true;
    }

    @Override
    public NDependencyFilter simplify() {
        return CoreFilterUtils.simplifyFilterOr(NDependencyFilter.class,this,all);
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrOr(Arrays.stream(all).map(NComplexExpressionString::toString).collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NDependencyFilterOr that = (NDependencyFilterOr) o;
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

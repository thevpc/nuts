package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.runtime.standalone.dependency.util.NComplexExpressionString;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.*;
import java.util.stream.Collectors;

public class NDefinitionFilterAnd extends DefinitionFilterBase implements NComplexExpressionString {

    private NDefinitionFilter[] all;

    public NDefinitionFilterAnd(NDefinitionFilter... all) {
        super(NFilterOp.AND);
        Set<NDefinitionFilter> valid = new LinkedHashSet<>();
        if (all != null) {
            for (NDefinitionFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NDefinitionFilter[0]);
    }

    public NDefinitionFilter[] getChildren() {
        return Arrays.copyOf(all, all.length);
    }

    @Override
    public boolean acceptDefinition(NDefinition id) {
        if (all.length == 0) {
            return true;
        }
        for (NDefinitionFilter filter : all) {
            if (!filter.acceptDefinition(id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NDefinitionFilter simplify() {
        return CoreFilterUtils.simplifyFilterAnd(NDefinitionFilter.class, this, all);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NDefinitionFilterAnd that = (NDefinitionFilterAnd) o;
        return Objects.deepEquals(all, that.all);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Arrays.hashCode(all));
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrAnd(Arrays.stream(all).map(NComplexExpressionString::toString).collect(Collectors.toList()));
    }

    public List<NFilter> subFilters() {
        return Arrays.asList(all);
    }
}

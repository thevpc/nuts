package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.runtime.standalone.dependency.util.NComplexExpressionString;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NDefinitionFilterNone extends DefinitionFilterBase {

    private NDefinitionFilter[] all;

    public NDefinitionFilterNone(NDefinitionFilter... all) {
        super(NFilterOp.NOT);
        List<NDefinitionFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NDefinitionFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NDefinitionFilter[0]);
    }

    @Override
    public boolean acceptDefinition(NDefinition id) {
        if (all.length == 0) {
            return true;
        }
        for (NDefinitionFilter filter : all) {
            if (filter.acceptDefinition(id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NDefinitionFilter simplify() {
        return CoreFilterUtils.simplifyFilterNone( NDefinitionFilter.class,this,all);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NDefinitionFilterNone that = (NDefinitionFilterNone) o;
        return Objects.deepEquals(all, that.all);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Arrays.hashCode(all));
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrNone(Arrays.stream(all).map(NComplexExpressionString::toString).collect(Collectors.toList()));
    }

    public List<NFilter> subFilters() {
        return Arrays.asList(all);
    }
}

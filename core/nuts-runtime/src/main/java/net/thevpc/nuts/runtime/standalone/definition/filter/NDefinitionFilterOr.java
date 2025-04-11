package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.NDefinition;
import net.thevpc.nuts.NDefinitionFilter;
import net.thevpc.nuts.runtime.standalone.dependency.util.NComplexExpressionString;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NDefinitionFilterOr extends AbstractDefinitionFilter implements NComplexExpressionString {

    private NDefinitionFilter[] all;

    public NDefinitionFilterOr(NDefinitionFilter... all) {
        super(NFilterOp.OR);
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

    public NDefinitionFilter[] getChildren() {
        return Arrays.copyOf(all, all.length);
    }

    @Override
    public boolean acceptDefinition(NDefinition id) {
        if (all.length == 0) {
            return true;
        }
        for (NDefinitionFilter filter : all) {
            if (filter.acceptDefinition(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NDefinitionFilter simplify() {
        return CoreFilterUtils.simplifyFilterOr(NDefinitionFilter.class, this, all);
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrOr(Arrays.stream(all).map(NComplexExpressionString::toString).collect(Collectors.toList()));
    }

    public List<NFilter> getSubFilters() {
        return Arrays.asList(all);
    }
}

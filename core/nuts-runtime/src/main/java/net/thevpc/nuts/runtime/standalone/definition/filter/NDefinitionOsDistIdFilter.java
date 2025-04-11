package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.dependency.filter.AbstractDependencyFilter;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NCoreCollectionUtils;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NStream;

import java.util.*;
import java.util.stream.Collectors;


public class NDefinitionOsDistIdFilter extends AbstractDefinitionFilter {

    private Set<NId> accepted = new HashSet<>();

    public NDefinitionOsDistIdFilter() {
        super(NFilterOp.CUSTOM);
    }

    public NDefinitionOsDistIdFilter(Collection<NId> accepted) {
        super(NFilterOp.CUSTOM);
        LinkedHashSet<NId> s2 = new LinkedHashSet<>();
        NCoreCollectionUtils.addAllNonNull(s2, accepted);
        this.accepted = new LinkedHashSet<>(s2);
    }


    @Override
    public boolean acceptDefinition(NDefinition def) {
        List<String> current = NStream.ofIterable(def.getDescriptor().getCondition().getOsDist()).filterNonBlank().toList();
        if(current.isEmpty() || accepted.isEmpty()){
            return true;
        }
        for (NId nutsId : accepted) {
            if(CoreFilterUtils.matchesOsDist(nutsId.toString(),current)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return
                CoreStringUtils.trueOrEqOrIn("osDist",
                        accepted.stream().map(x -> x.toString()).collect(Collectors.toList())
                )
                ;
    }

    @Override
    public NDefinitionFilter simplify() {
        return accepted.isEmpty() ? NDefinitionFilters.of().always() : this;
    }
}

package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.internal.rpi.NDefinitionFilterRPI;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.collections.NCollections;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.pipeline.NStream;

import java.util.*;
import java.util.stream.Collectors;


public class NDefinitionOsDistIdFilter extends DefinitionFilterBase {

    private Set<NId> accepted = new HashSet<>();

    public NDefinitionOsDistIdFilter() {
        super(NFilterOp.CUSTOM);
    }

    public NDefinitionOsDistIdFilter(Collection<NId> accepted) {
        super(NFilterOp.CUSTOM);
        LinkedHashSet<NId> s2 = new LinkedHashSet<>();
        NCollections.addAllNonNull(s2, accepted);
        this.accepted = new LinkedHashSet<>(s2);
    }


    @Override
    public boolean acceptDefinition(NDefinition def) {
        List<String> current = NStream.ofIterable(def.descriptor().condition().osDist()).nonBlank().toList();
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
        return accepted.isEmpty() ? NDefinitionFilterRPI.of().always() : this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NDefinitionOsDistIdFilter that = (NDefinitionOsDistIdFilter) o;
        return Objects.equals(accepted, that.accepted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accepted);
    }
}

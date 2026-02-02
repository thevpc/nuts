package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.artifact.NDefinitionFilters;
import net.thevpc.nuts.platform.NExecutionEngineFamily;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NStream;

import java.util.*;
import java.util.stream.Collectors;


public class NDefinitionPlatformFamilyFilter extends AbstractDefinitionFilter {

    private Set<NExecutionEngineFamily> accepted = new HashSet<>();

    public NDefinitionPlatformFamilyFilter(Collection<NExecutionEngineFamily> accepted) {
        super(NFilterOp.CUSTOM);
        LinkedHashSet<NExecutionEngineFamily> s2 = new LinkedHashSet<>();
        NCollections.addAllNonNull(s2, accepted);
        this.accepted = new LinkedHashSet<>(s2);
    }


    @Override
    public boolean acceptDefinition(NDefinition def) {
        List<NExecutionEngineFamily> current = NStream.ofIterable(def.getDescriptor().getCondition().getPlatform()).nonBlank()
                .map(x -> NExecutionEngineFamily.parse(x).orNull())
                .nonBlank()
                .toList();
        if (current.isEmpty() || accepted.isEmpty()) {
            return true;
        }
        for (NExecutionEngineFamily osf : accepted) {
            if (CoreFilterUtils.matchesEnum(osf, current)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return
                CoreStringUtils.trueOrEqOrIn("platformFamily",
                        accepted.stream().map(x -> x.toString()).collect(Collectors.toList())
                )
                ;
    }

    @Override
    public NDefinitionFilter simplify() {
        return accepted.isEmpty() ? NDefinitionFilters.of().always() : this;
    }
}

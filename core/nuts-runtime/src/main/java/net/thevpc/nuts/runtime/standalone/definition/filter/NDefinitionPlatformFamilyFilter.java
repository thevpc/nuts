package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NCoreCollectionUtils;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NStream;

import java.util.*;
import java.util.stream.Collectors;


public class NDefinitionPlatformFamilyFilter extends AbstractDefinitionFilter {

    private Set<NPlatformFamily> accepted = new HashSet<>();

    public NDefinitionPlatformFamilyFilter(Collection<NPlatformFamily> accepted) {
        super(NFilterOp.CUSTOM);
        LinkedHashSet<NPlatformFamily> s2 = new LinkedHashSet<>();
        NCoreCollectionUtils.addAllNonNull(s2, accepted);
        this.accepted = new LinkedHashSet<>(s2);
    }


    @Override
    public boolean acceptDefinition(NDefinition def) {
        List<NPlatformFamily> current = NStream.ofIterable(def.getDescriptor().getCondition().getPlatform()).filterNonBlank()
                .map(x -> NPlatformFamily.parse(x).orNull())
                .filterNonBlank()
                .toList();
        if (current.isEmpty() || accepted.isEmpty()) {
            return true;
        }
        for (NPlatformFamily osf : accepted) {
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

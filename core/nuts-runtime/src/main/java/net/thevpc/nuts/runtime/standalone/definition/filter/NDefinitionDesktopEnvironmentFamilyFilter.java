package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NCoreCollectionUtils;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NStream;

import java.util.*;
import java.util.stream.Collectors;


public class NDefinitionDesktopEnvironmentFamilyFilter extends AbstractDefinitionFilter {

    private Set<NDesktopEnvironmentFamily> accepted = new HashSet<>();

    public NDefinitionDesktopEnvironmentFamilyFilter(Collection<NDesktopEnvironmentFamily> accepted) {
        super(NFilterOp.CUSTOM);
        LinkedHashSet<NDesktopEnvironmentFamily> s2 = new LinkedHashSet<>();
        NCoreCollectionUtils.addAllNonNull(s2, accepted);
        this.accepted = new LinkedHashSet<>(s2);
    }


    @Override
    public boolean acceptDefinition(NDefinition def) {
        List<NDesktopEnvironmentFamily> current = NStream.ofIterable(def.getDescriptor().getCondition().getDesktopEnvironment()).filterNonBlank()
                .map(x -> NDesktopEnvironmentFamily.parse(x).orNull())
                .filterNonBlank()
                .toList();
        if (current.isEmpty() || accepted.isEmpty()) {
            return true;
        }
        for (NDesktopEnvironmentFamily osf : accepted) {
            if (CoreFilterUtils.matchesEnum(osf, current)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return
                CoreStringUtils.trueOrEqOrIn("desktopEnvironmentFamily",
                        accepted.stream().map(x -> x.toString()).collect(Collectors.toList())
                )
                ;
    }

    @Override
    public NDefinitionFilter simplify() {
        return accepted.isEmpty() ? NDefinitionFilters.of().always() : this;
    }
}

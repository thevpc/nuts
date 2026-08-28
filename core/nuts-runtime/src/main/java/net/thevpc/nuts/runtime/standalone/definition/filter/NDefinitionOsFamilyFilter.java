package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.internal.rpi.NDefinitionFilterRPI;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.collections.NCollections;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.pipeline.NStream;

import java.util.*;
import java.util.stream.Collectors;


public class NDefinitionOsFamilyFilter extends DefinitionFilterBase {

    private Set<NOsFamily> accepted = new HashSet<>();

    public NDefinitionOsFamilyFilter(Collection<NOsFamily> accepted) {
        super(NFilterOp.CUSTOM);
        LinkedHashSet<NOsFamily> s2 = new LinkedHashSet<>();
        NCollections.addAllNonNull(s2, accepted);
        this.accepted = new LinkedHashSet<>(s2);
    }


    @Override
    public boolean acceptDefinition(NDefinition def) {
        List<NOsFamily> current = NStream.ofIterable(def.descriptor().condition().os()).nonBlank()
                .map(x -> NOsFamily.parse(x).orNull())
                .nonBlank()
                .toList();
        if (current.isEmpty() || accepted.isEmpty()) {
            return true;
        }
        for (NOsFamily osf : accepted) {
            if (CoreFilterUtils.matchesEnum(osf, current)) {
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
        NDefinitionOsFamilyFilter that = (NDefinitionOsFamilyFilter) o;
        return Objects.equals(accepted, that.accepted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), accepted);
    }
}

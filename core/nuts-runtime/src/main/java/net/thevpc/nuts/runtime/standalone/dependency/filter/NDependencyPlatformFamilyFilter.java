package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.NPlatformFamily;
import net.thevpc.nuts.util.NCoreCollectionUtils;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class NDependencyPlatformFamilyFilter extends AbstractDependencyFilter  {

    private Set<NPlatformFamily> accepted = EnumSet.noneOf(NPlatformFamily.class);

    public NDependencyPlatformFamilyFilter() {
        super(NFilterOp.CUSTOM);
    }

    private NDependencyPlatformFamilyFilter(Collection<NPlatformFamily> accepted) {
        super(NFilterOp.CUSTOM);
        this.accepted = EnumSet.copyOf(accepted);
    }

    public NDependencyPlatformFamilyFilter(String accepted) {
        super(NFilterOp.CUSTOM);
        this.accepted = EnumSet.noneOf(NPlatformFamily.class);
        for (NId e : NId.getList(accepted).get()) {
            if (!e.isBlank()) {
                this.accepted.add(NPlatformFamily.parse(e.getArtifactId()).orNull());
            }
        }
    }

    public NDependencyPlatformFamilyFilter add(Collection<NPlatformFamily> oses) {
        EnumSet<NPlatformFamily> s2 = EnumSet.copyOf(this.accepted);
        NCoreCollectionUtils.addAllNonNull(s2, oses);
        return new NDependencyPlatformFamilyFilter(s2);
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        List<String> current = dependency.getCondition().getPlatform();
        boolean empty = true;
        if (current != null) {
            for (String e : current) {
                if (!e.isEmpty()) {
                    empty = false;
                    if (accepted.contains(NPlatformFamily.parse(e).orNull())) {
                        return true;
                    }
                }
            }
        }
        return empty;
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrEqOrIn("platform",
                        accepted.stream().map(x -> x.id()).collect(Collectors.toList())
                );
    }

    @Override
    public NDependencyFilter simplify() {
        return accepted.isEmpty() ? NDependencyFilters.of().always() : this;
    }
}

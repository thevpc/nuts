package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;


public class NutsDependencyPlatformFamilyFilter extends AbstractDependencyFilter  {

    private Set<NutsPlatformFamily> accepted = EnumSet.noneOf(NutsPlatformFamily.class);

    public NutsDependencyPlatformFamilyFilter(NutsSession session) {
        super(session, NutsFilterOp.CUSTOM);
    }

    private NutsDependencyPlatformFamilyFilter(NutsSession session, Collection<NutsPlatformFamily> accepted) {
        super(session, NutsFilterOp.CUSTOM);
        this.accepted = EnumSet.copyOf(accepted);
    }

    public NutsDependencyPlatformFamilyFilter(NutsSession session, String accepted) {
        super(session, NutsFilterOp.CUSTOM);
        this.accepted = EnumSet.noneOf(NutsPlatformFamily.class);
        for (String e : StringTokenizerUtils.splitDefault(accepted)) {
            if (!e.isEmpty()) {
                this.accepted.add(NutsPlatformFamily.parseLenient(e));
            }
        }
    }

    public NutsDependencyPlatformFamilyFilter add(Collection<NutsPlatformFamily> os) {
        EnumSet<NutsPlatformFamily> s2 = EnumSet.copyOf(this.accepted);
        s2.addAll(os);
        return new NutsDependencyPlatformFamilyFilter(getSession(), s2);
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        String[] current = dependency.getCondition().getPlatform();
        boolean empty = true;
        if (current != null) {
            for (String e : current) {
                if (!e.isEmpty()) {
                    empty = false;
                    if (accepted.contains(NutsPlatformFamily.parseLenient(e))) {
                        return true;
                    }
                }
            }
        }
        return empty;
    }

    @Override
    public String toString() {
        return accepted.isEmpty() ? "true" : "os in (" + accepted.stream().map(NutsPlatformFamily::id).collect(Collectors.joining(", ")) + ')';
    }

    @Override
    public NutsDependencyFilter simplify() {
        return accepted.isEmpty() ? NutsDependencyFilters.of(getSession()).always() : this;
    }
}

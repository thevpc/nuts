package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class NutsDependencyDEFilter extends AbstractDependencyFilter  {

    private Set<NutsDesktopEnvironmentFamily> accepted = EnumSet.noneOf(NutsDesktopEnvironmentFamily.class);

    public NutsDependencyDEFilter(NutsSession session) {
        super(session, NutsFilterOp.CUSTOM);
    }

    private NutsDependencyDEFilter(NutsSession session, Collection<NutsDesktopEnvironmentFamily> accepted) {
        super(session, NutsFilterOp.CUSTOM);
        this.accepted = EnumSet.copyOf(accepted);
    }

    public NutsDependencyDEFilter(NutsSession session, String accepted) {
        super(session, NutsFilterOp.CUSTOM);
        this.accepted = EnumSet.noneOf(NutsDesktopEnvironmentFamily.class);
        for (String e : StringTokenizerUtils.splitDefault(accepted)) {
            if (!e.isEmpty()) {
                this.accepted.add(NutsDesktopEnvironmentFamily.parse(e).orNull());
            }
        }
    }

    public NutsDependencyDEFilter add(Collection<NutsDesktopEnvironmentFamily> os) {
        EnumSet<NutsDesktopEnvironmentFamily> s2 = EnumSet.copyOf(this.accepted);
        s2.addAll(os);
        return new NutsDependencyDEFilter(getSession(), s2);
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        List<String> current = dependency.getCondition().getDesktopEnvironment();
        boolean empty = true;
        if (current != null) {
            for (String e : current) {
                if (!e.isEmpty()) {
                    empty = false;
                    if (accepted.contains(NutsDesktopEnvironmentFamily.parse(e).orNull())) {
                        return true;
                    }
                }
            }
        }
        return empty;
    }

    @Override
    public String toString() {
        return accepted.isEmpty() ? "true" : "desktopEnvironment in (" + accepted.stream().map(NutsDesktopEnvironmentFamily::id).collect(Collectors.joining(", ")) + ')';
    }

    @Override
    public NutsDependencyFilter simplify() {
        return accepted.isEmpty() ? NutsDependencyFilters.of(getSession()).always() : this;
    }
}

package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NDesktopEnvironmentFamily;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class NDependencyDEFilter extends AbstractDependencyFilter  {

    private Set<NDesktopEnvironmentFamily> accepted = EnumSet.noneOf(NDesktopEnvironmentFamily.class);

    public NDependencyDEFilter() {
        super(NFilterOp.CUSTOM);
    }

    private NDependencyDEFilter(Collection<NDesktopEnvironmentFamily> accepted) {
        super(NFilterOp.CUSTOM);
        this.accepted = EnumSet.copyOf(accepted);
    }

    public NDependencyDEFilter(String accepted) {
        super(NFilterOp.CUSTOM);
        this.accepted = EnumSet.noneOf(NDesktopEnvironmentFamily.class);
        for (String e : StringTokenizerUtils.splitDefault(accepted)) {
            if (!e.isEmpty()) {
                this.accepted.add(NDesktopEnvironmentFamily.parse(e).orNull());
            }
        }
    }

    public NDependencyDEFilter add(Collection<NDesktopEnvironmentFamily> os) {
        EnumSet<NDesktopEnvironmentFamily> s2 = EnumSet.copyOf(this.accepted);
        s2.addAll(os);
        return new NDependencyDEFilter(s2);
    }

    @Override
    public boolean acceptDependency(NId from, NDependency dependency) {
        List<String> current = dependency.getCondition().getDesktopEnvironment();
        boolean empty = true;
        if (current != null) {
            for (String e : current) {
                if (!e.isEmpty()) {
                    empty = false;
                    if (accepted.contains(NDesktopEnvironmentFamily.parse(e).orNull())) {
                        return true;
                    }
                }
            }
        }
        return empty;
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrEqOrIn("desktopEnvironment",
                        accepted.stream().map(NDesktopEnvironmentFamily::id).collect(Collectors.toList())
                        );
    }

    @Override
    public NDependencyFilter simplify() {
        return accepted.isEmpty() ? NDependencyFilters.of().always() : this;
    }
}

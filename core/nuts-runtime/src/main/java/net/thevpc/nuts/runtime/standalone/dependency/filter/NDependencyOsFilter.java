package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.NOsFamily;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class NDependencyOsFilter extends AbstractDependencyFilter  {

    private Set<NOsFamily> os = EnumSet.noneOf(NOsFamily.class);

    public NDependencyOsFilter(NWorkspace workspace) {
        super(workspace, NFilterOp.CUSTOM);
    }

    private NDependencyOsFilter(NWorkspace workspace, Collection<NOsFamily> os) {
        super(workspace, NFilterOp.CUSTOM);
        this.os = EnumSet.copyOf(os);
    }

    public NDependencyOsFilter(NWorkspace workspace, String os) {
        super(workspace, NFilterOp.CUSTOM);
        this.os = EnumSet.noneOf(NOsFamily.class);
        for (String e : StringTokenizerUtils.splitDefault(os)) {
            if (!e.isEmpty()) {
                this.os.add(NOsFamily.parse(e).orNull());
            }
        }
    }

    public NDependencyOsFilter add(Collection<NOsFamily> os) {
        EnumSet<NOsFamily> s2 = EnumSet.copyOf(this.os);
        s2.addAll(os);
        return new NDependencyOsFilter(workspace, s2);
    }

    @Override
    public boolean acceptDependency(NId from, NDependency dependency) {
        List<String> current = dependency.getCondition().getOs();
        boolean empty = true;
        if (current != null) {
            for (String e : current) {
                if (!e.isEmpty()) {
                    empty = false;
                    if (os.contains(NOsFamily.parse(e).orNull())) {
                        return true;
                    }
                }
            }
        }
        return empty;
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrEqOrIn("os",
                        os.stream().map(x -> x.id()).collect(Collectors.toList())
                );
    }

    @Override
    public NDependencyFilter simplify() {
        return os.isEmpty() ? NDependencyFilters.of().always() : this;
    }
}

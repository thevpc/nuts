package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.NArchFamily;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class NDependencyArchFamilyFilter extends AbstractDependencyFilter {

    private Set<NArchFamily> archs = EnumSet.noneOf(NArchFamily.class);

    public NDependencyArchFamilyFilter(NWorkspace workspace) {
        super(workspace, NFilterOp.CUSTOM);
    }

    private NDependencyArchFamilyFilter(NWorkspace workspace, Collection<NArchFamily> os) {
        super(workspace, NFilterOp.CUSTOM);
        this.archs = EnumSet.copyOf(os);
    }

    public NDependencyArchFamilyFilter(NWorkspace workspace, String os) {
        super(workspace, NFilterOp.CUSTOM);
        this.archs = EnumSet.noneOf(NArchFamily.class);
        for (String e : StringTokenizerUtils.splitDefault( os)) {
            if (!e.isEmpty()) {
                this.archs.add(NArchFamily.parse(e).orElse(NArchFamily.UNKNOWN));
            }
        }
    }

    public NDependencyArchFamilyFilter add(Collection<NArchFamily> os) {
        EnumSet<NArchFamily> s2 = EnumSet.copyOf(this.archs);
        s2.addAll(os);
        return new NDependencyArchFamilyFilter(workspace, s2);
    }

    @Override
    public boolean acceptDependency(NId from, NDependency dependency) {
        List<String> current = dependency.getCondition().getArch();
        boolean empty = true;
        if (current != null) {
            for (String e : current) {
                if (!e.isEmpty()) {
                    empty = false;
                    if (archs.contains(NArchFamily.parse(e).orElse(NArchFamily.UNKNOWN))) {
                        return true;
                    }
                }
            }
        }
        return empty;
    }

    @Override
    public String toString() {
        return
                CoreStringUtils.trueOrEqOrIn("arch",
                        archs.stream().map(x -> x.id()).collect(Collectors.toList())
                );
    }

    @Override
    public NDependencyFilter simplify() {
        return archs.isEmpty() ? NDependencyFilters.of().always() : this;
    }
}

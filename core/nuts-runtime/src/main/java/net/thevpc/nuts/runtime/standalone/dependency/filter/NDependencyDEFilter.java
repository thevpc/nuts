package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NDesktopEnvironmentFamily;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.util.NCoreCollectionUtils;
import net.thevpc.nuts.util.NFilterOp;

import java.util.*;
import java.util.stream.Collectors;


public class NDependencyDEFilter extends AbstractDependencyFilter {

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

    public NDependencyDEFilter add(Collection<NDesktopEnvironmentFamily> oses) {
        EnumSet<NDesktopEnvironmentFamily> s2 = EnumSet.copyOf(this.accepted);
        NCoreCollectionUtils.addAllNonNull(s2, oses);
        return new NDependencyDEFilter(s2);
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        if (accepted.isEmpty()) {
            return true;
        }
        List<String> current = dependency.getCondition().getDesktopEnvironment();
        if (current != null) {
            List<NDesktopEnvironmentFamily> currentFamilies = current.stream().map(e -> {
                if (!e.isEmpty()) {
                    NId de = NId.get(e).orNull();
                    if (de != null) {
                        return NDesktopEnvironmentFamily.parse(de.getArtifactId()).orNull();
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            if (currentFamilies.isEmpty()) {
                return true;
            }
            for (NDesktopEnvironmentFamily currentFamily : currentFamilies) {
                if (accepted.contains(currentFamily)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
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

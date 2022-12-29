package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class NDependencyPlatformFamilyFilter extends AbstractDependencyFilter  {

    private Set<NPlatformFamily> accepted = EnumSet.noneOf(NPlatformFamily.class);

    public NDependencyPlatformFamilyFilter(NSession session) {
        super(session, NFilterOp.CUSTOM);
    }

    private NDependencyPlatformFamilyFilter(NSession session, Collection<NPlatformFamily> accepted) {
        super(session, NFilterOp.CUSTOM);
        this.accepted = EnumSet.copyOf(accepted);
    }

    public NDependencyPlatformFamilyFilter(NSession session, String accepted) {
        super(session, NFilterOp.CUSTOM);
        this.accepted = EnumSet.noneOf(NPlatformFamily.class);
        for (NId e : NId.ofList(accepted).get(session)) {
            if (!e.isBlank()) {
                this.accepted.add(NPlatformFamily.parse(e.getArtifactId()).orNull());
            }
        }
    }

    public NDependencyPlatformFamilyFilter add(Collection<NPlatformFamily> os) {
        EnumSet<NPlatformFamily> s2 = EnumSet.copyOf(this.accepted);
        s2.addAll(os);
        return new NDependencyPlatformFamilyFilter(getSession(), s2);
    }

    @Override
    public boolean acceptDependency(NId from, NDependency dependency, NSession session) {
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
        return accepted.isEmpty() ? "true" : "os in (" + accepted.stream().map(NPlatformFamily::id).collect(Collectors.joining(", ")) + ')';
    }

    @Override
    public NDependencyFilter simplify() {
        return accepted.isEmpty() ? NDependencyFilters.of(getSession()).always() : this;
    }
}

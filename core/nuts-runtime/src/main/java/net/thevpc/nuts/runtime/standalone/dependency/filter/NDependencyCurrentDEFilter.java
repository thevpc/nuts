package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.platform.NDesktopEnvironmentFamily;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.util.NFilterOp;

import java.util.List;
import java.util.Objects;

/**
 * Accepts dependencies matching the current desktop environment, like
 * {@code byDesktop(NEnv.of().desktopEnvironmentFamilies())}, but only detects
 * the desktop environment when a dependency actually declares a desktop
 * condition. That detection loads AWT, and the runnable filter built on every
 * workspace boot would otherwise pay for it even though almost no dependency
 * carries such a condition.
 */
public class NDependencyCurrentDEFilter extends NDependencyFilterBase {

    private volatile NDependencyFilter resolved;

    public NDependencyCurrentDEFilter() {
        super(NFilterOp.CUSTOM);
    }

    private NDependencyFilter resolved() {
        NDependencyFilter r = resolved;
        if (r == null) {
            r = new NDependencyDEFilter()
                    .add(NEnv.of().desktopEnvironmentFamilies())
                    .simplify();
            resolved = r;
        }
        return r;
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        List<String> de = dependency.condition().desktopEnvironment();
        if (de == null || de.stream().allMatch(e -> e == null || e.isEmpty())) {
            return true;
        }
        return resolved().acceptDependency(dependency, from);
    }

    @Override
    public String toString() {
        return resolved().toString();
    }

    @Override
    public NDependencyFilter simplify() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), NDependencyCurrentDEFilter.class);
    }
}

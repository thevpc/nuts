package net.thevpc.nuts.runtime;

import net.thevpc.nuts.NutsTokenFilter;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;

public class DefaultNutsTokenFilter implements NutsTokenFilter {

    protected String expression;

    public DefaultNutsTokenFilter(String expression) {
        this.expression = expression;
    }

    @Override
    public boolean isNull() {
        return expression == null;
    }

    @Override
    public boolean isBlank() {
        return expression == null || expression.trim().isEmpty();
    }

    @Override
    public boolean like(String pattern) {
        return matches(CoreStringUtils.simpexpToRegexp(pattern));
    }

    @Override
    public boolean matches(String pattern) {
        return (expression == null ? "" : expression).matches((pattern == null ? ".*" : pattern));
    }

    @Override
    public boolean contains(String substring) {
        return (expression == null ? "" : expression).matches((substring == null ? "" : substring));
    }
}

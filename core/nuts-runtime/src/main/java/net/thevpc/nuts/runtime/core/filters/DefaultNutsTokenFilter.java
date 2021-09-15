//package net.thevpc.nuts.runtime.core.filters;
//
//import net.thevpc.nuts.NutsTokenFilter;
//import net.thevpc.nuts.runtime.bundles.string.GlobUtils;
//import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
//
//public class DefaultNutsTokenFilter implements NutsTokenFilter {
//
//    protected String expression;
//
//    public DefaultNutsTokenFilter(String expression) {
//        this.expression = expression;
//    }
//
//    @Override
//    public boolean isNull() {
//        return expression == null;
//    }
//
//    @Override
//    public boolean isBlank() {
//        return expression == null || expression.trim().isEmpty();
//    }
//
//    @Override
//    public boolean like(String pattern) {
//        return GlobUtils.ofExact(pattern).matcher(expression == null ? "" : expression).matches();
//    }
//
//    @Override
//    public boolean matches(String pattern) {
//        return (expression == null ? "" : expression).matches((pattern == null ? ".*" : pattern));
//    }
//
//    @Override
//    public boolean contains(String substring) {
//        return (expression == null ? "" : expression).matches((substring == null ? "" : substring));
//    }
//}

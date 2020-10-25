package net.vpc.app.nuts.runtime.parser;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.NutsDependencyScopes;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

public class DefaultNutsDependencyParser implements NutsDependencyParser {
    private NutsWorkspace ws;
    private boolean lenient=true;

    public DefaultNutsDependencyParser(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsDependencyParser setLenient(boolean lenient) {
        this.lenient=lenient;
        return this;
    }

    @Override
    public boolean isLenient() {
        return lenient;
    }

    @Override
    public NutsDependency parseDependency(String dependency) {
        NutsDependency d = CoreNutsUtils.parseNutsDependency(ws, dependency);
        if (d == null && !isLenient()) {
            throw new NutsParseException(ws, "Invalid Dependency format : " + dependency);
        }
        return d;
    }

    @Override
    public NutsDependencyScope parseScope(String scope) {
        return NutsDependencyScopes.parseScope(scope,isLenient());
    }

    @Override
    public boolean parseOptional(String optional) {
        if(CoreStringUtils.isBlank(optional)){
            return false;
        }
        return "true".equals(optional.trim());
    }
}

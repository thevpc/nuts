package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.util.*;

public class DefaultNutsExpr implements NutsExpr {

    private final NutsSession session;

    public DefaultNutsExpr(NutsSession session) {
        this.session = session;
    }

    public NutsExprDeclarations newDeclarations(boolean includeDefaults) {
        return includeDefaults ? new DefaultRootDeclarations(session) : new EmptyRootDeclarations(session);
    }

    public NutsExprDeclarations newDeclarations(boolean includeDefaults, NutsExprEvaluator evaluator) {
        NutsExprDeclarations r = newDeclarations(includeDefaults);
        if (evaluator != null) {
            r = r.newDeclarations(evaluator);
        }
        return r;
    }

    @Override
    public NutsExprMutableDeclarations newMutableDeclarations(boolean includeDefaults) {
        return newDeclarations(includeDefaults).newMutableDeclarations();
    }

    public NutsExprMutableDeclarations newMutableDeclarations(boolean includeDefaults, NutsExprEvaluator evaluator) {
        return newDeclarations(includeDefaults, evaluator).newMutableDeclarations();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}


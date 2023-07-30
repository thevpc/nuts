package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.*;
import net.thevpc.nuts.expr.NExpr;
import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprEvaluator;
import net.thevpc.nuts.expr.NExprMutableDeclarations;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class DefaultNExpr implements NExpr {

    private final NSession session;

    public DefaultNExpr(NSession session) {
        this.session = session;
    }

    public NExprDeclarations newDeclarations(boolean includeDefaults) {
        return includeDefaults ? new DefaultRootDeclarations(session) : new EmptyRootDeclarations(session);
    }

    public NExprDeclarations newDeclarations(boolean includeDefaults, NExprEvaluator evaluator) {
        NExprDeclarations r = newDeclarations(includeDefaults);
        if (evaluator != null) {
            r = r.newDeclarations(evaluator);
        }
        return r;
    }

    @Override
    public NExprMutableDeclarations newMutableDeclarations(boolean includeDefaults) {
        return newDeclarations(includeDefaults).newMutableDeclarations();
    }

    public NExprMutableDeclarations newMutableDeclarations(boolean includeDefaults, NExprEvaluator evaluator) {
        return newDeclarations(includeDefaults, evaluator).newMutableDeclarations();
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}


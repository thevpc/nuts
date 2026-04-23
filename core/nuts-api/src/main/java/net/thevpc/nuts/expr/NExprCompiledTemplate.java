package net.thevpc.nuts.expr;

import net.thevpc.nuts.io.NOutputTarget;

import java.io.OutputStream;
import java.io.Writer;

public interface NExprCompiledTemplate {
    void run(Writer target, NExprContext context);
    void run(OutputStream target, NExprContext context);
    void run(NOutputTarget target, NExprContext context);
    String runString(NExprContext context);

    void run(Writer target);
    void run(OutputStream target);
    void run(NOutputTarget target);
    String runString();
}

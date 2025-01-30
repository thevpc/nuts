package net.thevpc.nuts.expr;

import net.thevpc.nuts.io.NOutputTarget;

import java.io.OutputStream;
import java.io.Writer;

public interface NExprCompiledTemplate {
    void run(Writer target, NExprDeclarations context);
    void run(OutputStream target, NExprDeclarations context);
    void run(NOutputTarget target, NExprDeclarations context);
    String runString(NExprDeclarations context);

    void run(Writer target);
    void run(OutputStream target);
    void run(NOutputTarget target);
    String runString();
}

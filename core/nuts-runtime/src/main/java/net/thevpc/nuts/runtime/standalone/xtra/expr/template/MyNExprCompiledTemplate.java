package net.thevpc.nuts.runtime.standalone.xtra.expr.template;

import net.thevpc.nuts.expr.NExprCompiledTemplate;
import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NOutputTarget;

import java.io.*;

class MyNExprCompiledTemplate implements NExprCompiledTemplate {
    private final ProcessStreamContext ctx;
    private final TagNode n;

    public MyNExprCompiledTemplate(ProcessStreamContext ctx, TagNode n) {
        this.ctx = ctx;
        this.n = n;
    }

    @Override
    public void run(Writer target) {
        run(target, ctx.context);
    }

    @Override
    public void run(OutputStream target) {
        run(target, ctx.context);
    }

    @Override
    public void run(NOutputTarget target) {
        run(target, ctx.context);
    }

    @Override
    public String runString() {
        return runString(ctx.context);
    }

    @Override
    public void run(Writer target, NExprDeclarations context) {
        ProcessStreamContext ctx = this.ctx.copy();
        ctx.out = new BufferedWriter(target);
        if (context != null) {
            ctx.context = context;
        }
        try {
            if (n != null) {
                n.run(ctx);
            }
            ctx.out.flush();
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    @Override
    public void run(OutputStream target, NExprDeclarations context) {
        ProcessStreamContext ctx = this.ctx.copy();
        ctx.out = new BufferedWriter(new OutputStreamWriter(target));
        if (context != null) {
            ctx.context = context;
        }
        try {
            if (n != null) {
                n.run(ctx);
            }
            ctx.out.flush();
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    @Override
    public void run(NOutputTarget target, NExprDeclarations context) {
        try (OutputStream w = target.getOutputStream()) {
            run(w, context);
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    @Override
    public String runString(NExprDeclarations context) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        run(writer, context);
        try {
            writer.flush();
        } catch (IOException e) {
            throw new NIOException(e);
        }
        return out.toString();
    }
}

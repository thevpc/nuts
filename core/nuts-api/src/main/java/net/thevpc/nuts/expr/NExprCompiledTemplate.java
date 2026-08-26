package net.thevpc.nuts.expr;

import net.thevpc.nuts.io.NOutputTarget;

import java.io.OutputStream;
import java.io.Writer;

/**
 * NExprCompiledTemplate interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprCompiledTemplate {
    /**
     * Run.
     *
     * @param target target
     * @param context context
     */
    void run(Writer target, NExprContext context);
    /**
     * Run.
     *
     * @param target target
     * @param context context
     */
    void run(OutputStream target, NExprContext context);
    /**
     * Run.
     *
     * @param target target
     * @param context context
     */
    void run(NOutputTarget target, NExprContext context);
    /**
     * Run string.
     *
     * @param context context
     * @return run string result
     */
    String runString(NExprContext context);

    /**
     * Run.
     *
     * @param target target
     */
    void run(Writer target);
    /**
     * Run.
     *
     * @param target target
     */
    void run(OutputStream target);
    /**
     * Run.
     *
     * @param target target
     */
    void run(NOutputTarget target);
    /**
     * Run string.
     *
     * @return run string result
     */
    String runString();
}

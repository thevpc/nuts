package net.thevpc.nuts.expr;

import net.thevpc.nuts.internal.rpi.NExprRPI;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

/**
 * NExprNode interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprNode {
    /**
     * Creates a new instance of of word.
     *
     * @param name name
     * @return of word result
     */
    static NExprWordNode ofWord(String name) {
        return NExprRPI.of().createExprWordNode(name);
    }

    /**
     * Creates a new instance of of literal.
     *
     * @param name name
     * @return of literal result
     */
    static NExprLiteralNode ofLiteral(Object name) {
        return NExprRPI.of().createExprLiteralNode(name);
    }

    /**
     * Eval.
     *
     * @param context context
     * @return eval result
     */
    NOptional<Object> eval(NExprContext context);

    /**
     * Node type.
     *
     * @return node type result
     */
    NExprNodeType nodeType();

    /**
     * Children.
     *
     * @return children result
     */
    List<NExprNode> children();

    /**
     * Name.
     *
     * @return name result
     */
    String name();
}

package net.thevpc.nuts.expr;

import net.thevpc.nuts.util.NOptional;

import java.util.List;

/**
 * NExprCallContext interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprCallContext {
    /**
     * Name.
     *
     * @return name result
     */
    String name();

    /**
     * Args.
     *
     * @return args result
     */
    List<NExprNodeValue> args();

    /**
     * Arg.
     *
     * @param index index
     * @return arg result
     */
    NOptional<NExprNodeValue> arg(int index);

    /**
     * Context.
     *
     * @return context result
     */
    NExprContext context();

    /**
     * Context type.
     *
     * @return context type result
     */
    NExprCallContextType contextType();

    /**
     * Fixity.
     *
     * @return fixity result
     */
    NFixity fixity();

    /**
     * Precedence.
     *
     * @return precedence result
     */
    int precedence();

    /**
     * Associativity.
     *
     * @return associativity result
     */
    NOperatorAssociativity associativity();
}

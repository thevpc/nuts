package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.text.NMsg;

import java.util.*;

public class DefaultNExprElementReshaper implements NExprElementReshaper {
    private final Map<NOperatorSymbol, Integer> precedence = new HashMap<>();
    private final Map<NOperatorSymbol, NOperatorAssociativity> associativity = new HashMap<>();

    public DefaultNExprElementReshaper(Map<NOperatorSymbol, Integer> precedence, Map<NOperatorSymbol, NOperatorAssociativity> associativity) {
        this.precedence.putAll(precedence);
        this.associativity.putAll(associativity);
    }

    @Override
    public NExprElementReshaperBuilder builder() {
        return new DefaultNExprElementReshaperBuilder(precedence, associativity);
    }

    public boolean isUnaryContext(List<NElement> tokens, int index) {
        if (index == 0) return true;
        NElement prev = tokens.get(index - 1);
        return (prev instanceof NOperatorSymbolElement);
    }

    public int getPrecedence(OperatorToken op) {
        Integer p = precedence.get(op.symbol);
        return p != null ? p : 0;
    }

    protected int comparePrecedence(OperatorToken a, OperatorToken b) {
        return Integer.compare(getPrecedence(a), getPrecedence(b));
    }

    // ===== Internal Token Wrapper =====

    protected class OperatorToken {
        final NOperatorSymbol symbol;
        final NElement token;
        final NOperatorAssociativity assoc;
        final boolean unary;

        OperatorToken(NOperatorSymbol symbol, NElement token, boolean unary) {
            this.symbol = symbol;
            this.token = token;
            this.unary = unary;
            this.assoc = associativity.getOrDefault(symbol,
                    unary ? NOperatorAssociativity.RIGHT : NOperatorAssociativity.LEFT);
        }

        boolean isLeftParen() {
            return false; // extend later if needed
        }
    }


    @Override
    public NElement reshape(NFlatExprElement flat) {
        if (flat.isEmpty()) {
            return flat.builder()
                    .addDiagnostic(NElement.ofDiagnosticBuilder().setMessage(NMsg.ofC("Empty expression")).build())
                    .build();
        }

        Deque<NElement> output = new ArrayDeque<>();
        Deque<OperatorToken> operatorStack = new ArrayDeque<>();

        List<NElement> tokens = flat.children();
        for (int i = 0; i < tokens.size(); i++) {
            NElement token = tokens.get(i);

            if (token instanceof NOperatorSymbolElement) {
                NOperatorSymbol sym = ((NOperatorSymbolElement) token).symbol();
                boolean isUnary = isUnaryContext(tokens, i);

                OperatorToken opToken = new OperatorToken(sym, token, isUnary);

                while (!operatorStack.isEmpty()) {
                    OperatorToken top = operatorStack.peek();
                    if (top.isLeftParen()) break;

                    int cmp = comparePrecedence(top, opToken);
                    if (cmp > 0 || (cmp == 0 && opToken.assoc == NOperatorAssociativity.LEFT)) {
                        NElement applied = popOperator(operatorStack.pop(), output);
                        if (applied instanceof NEmptyElement) {
                            return flat.builder()
                                    .addDiagnostic(NElement.ofDiagnosticBuilder().setMessage(NMsg.ofC("Empty token")).build())
                                    .build();
                        }
                        output.push(applied);
                    } else {
                        break;
                    }
                }
                operatorStack.push(opToken);
            } else {
                // Operand: literals, objects, arrays, etc.
                output.push(token);
            }
        }

        // Pop remaining operators
        while (!operatorStack.isEmpty()) {
            OperatorToken op = operatorStack.pop();
            if (op.isLeftParen()) {
                return flat.builder()
                        .addDiagnostic(NElement.ofDiagnosticBuilder().setMessage(NMsg.ofC("Mismatched parentheses")).build())
                        .build();
            }
            NElement applied = popOperator(op, output);
            if (applied instanceof NEmptyElement) {
                return flat.builder()
                        .addDiagnostic(NElement.ofDiagnosticBuilder().setMessage(NMsg.ofC("Empty token")).build())
                        .build();
            }
            output.push(applied);
        }

        if (output.size() != 1) {
            return flat.builder()
                    .addDiagnostic(NElement.ofDiagnosticBuilder().setMessage(NMsg.ofC("Invalid expression: expected single result")).build())
                    .build();
        }

        return output.pop();
    }

    // ===== Helper Methods =====

    private NElement popOperator(OperatorToken op, Deque<NElement> output) {
        try {
            if (op.unary) {
                if (output.isEmpty()) {
                    return NElement.ofEmptyBuilder()
                            .addDiagnostic(NElement.ofDiagnosticBuilder().setMessage(NMsg.ofC("Missing operand for unary operator '%s'", op.symbol.lexeme())).build())
                            .build();
                }
                NElement operand = output.pop();
                return NElement.ofExprBuilder()
                        .symbol(op.symbol)
                        .position(NOperatorPosition.PREFIX)
                        .first(operand)
                        .build();
            } else {
                if (output.size() < 2) {
                    return NElement.ofEmptyBuilder()
                            .addDiagnostic(NElement.ofDiagnosticBuilder().setMessage(NMsg.ofC("Missing operands for binary operator '%s'", op.symbol.lexeme())).build())
                            .build();
                }
                NElement right = output.pop();
                NElement left = output.pop();
                return NElement.ofExprBuilder()
                        .symbol(op.symbol)
                        .position(NOperatorPosition.INFIX)
                        .first(left)
                        .second(right)
                        .build();
            }
        } catch (Exception ex) {
            return NElement.ofEmptyBuilder()
                    .addDiagnostic(NElement.ofDiagnosticBuilder().setMessage(NMsg.ofC("Operator application failed: %s", ex)).build())
                    .build();
        }
    }
}

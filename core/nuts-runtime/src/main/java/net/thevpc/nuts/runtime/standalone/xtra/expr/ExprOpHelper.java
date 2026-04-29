package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.expr.NOperatorAssociativity;
import net.thevpc.nuts.expr.NExprOpPrecedence;
import net.thevpc.nuts.expr.NExprOpType;
import net.thevpc.nuts.util.NAssert;

public class ExprOpHelper {
    public static NExprOpType resolveOpDefaultType(String name, NExprOpType type) {
        if (type != null) {
            return type;
        }
        switch (name) {
            case "+":
            case "-":
            case "*":
            case "/":
            case "%":
            case ".":
            case "?.":
            case "<<":
            case ">>":
            case ">>>":
            case "<<<":
            case "<":
            case ">":
            case "<=":
            case ">=":
            case "=":
            case "==":
            case "!=":
            case "<>":
            case "&&":
            case "&":
            case "||":
            case "|":
            case "??":
            {
                return NExprOpType.INFIX;
            }
            case "(":
            case "()":
            case "[":
            case "[]":
            case "{":
            case "{}":
            {
                return NExprOpType.POSTFIX;
            }
        }
        throw new IllegalArgumentException("unsupported operator " + name);
    }

    public static int resolveOpPrecedence(String name, NExprOpType type, int precedence) {
        NAssert.requireNamedNonNull(name, "name");
        NAssert.requireNamedNonNull(type, "type");
        if (precedence >= 0) {
            return precedence;
        }
        switch (name) {
            case "+": {
                switch (type) {
                    case INFIX: {
                        return NExprOpPrecedence.PLUS;
                    }
                }
                break;
            }
            case "-": {
                switch (type) {
                    case INFIX: {
                        return NExprOpPrecedence.MINUS;
                    }
                }
                break;
            }
            case "*": {
                switch (type) {
                    case INFIX: {
                        return NExprOpPrecedence.MUL;
                    }
                }
                break;
            }
            case "/": {
                switch (type) {
                    case INFIX: {
                        return NExprOpPrecedence.DIV;
                    }
                }
                break;
            }
            case "%": {
                if (type == null) {
                    type = NExprOpType.INFIX;
                }
                switch (type) {
                    case INFIX: {
                        return NExprOpPrecedence.MOD;
                    }
                }
                break;
            }
            case "(":
            case "()": {
                switch (type) {
                    case POSTFIX: {
                        return NExprOpPrecedence.PARS;
                    }
                }
                break;
            }
            case "[":
            case "[]": {
                switch (type) {
                    case POSTFIX: {
                        return NExprOpPrecedence.BRACKETS;
                    }
                }
                break;
            }
            case "{":
            case "{}": {
                switch (type) {
                    case POSTFIX: {
                        return NExprOpPrecedence.BRACES;
                    }
                }
                break;
            }
            case ".":
            case "?.": {
                if (type == null) {
                    type = NExprOpType.INFIX;
                }
                switch (type) {
                    case INFIX: {
                        return NExprOpPrecedence.DOT;
                    }
                }
                break;
            }
            case "<<":
            case ">>":
            case ">>>":
            case "<<<": {
                switch (type) {
                    case INFIX: {
                        return NExprOpPrecedence.SHIFT;
                    }
                }
                break;
            }
            case "<":
            case ">":
            case "<=":
            case ">=": {
                switch (type) {
                    case INFIX: {
                        return NExprOpPrecedence.CMP;
                    }
                }
                break;
            }
            case "=":
            case "==":
            case "!=":
            case "<>": {
                if (type == null) {
                    type = NExprOpType.INFIX;
                }
                switch (type) {
                    case INFIX: {
                        return NExprOpPrecedence.EQ;
                    }
                }
                break;
            }
            case "&&": {
                switch (type) {
                    case INFIX: {
                        return NExprOpPrecedence.AND;
                    }
                }
                break;
            }
            case "&": {
                switch (type) {
                    case INFIX: {
                        return NExprOpPrecedence.AMP;
                    }
                }
                break;
            }
            case "||": {
                switch (type) {
                    case INFIX: {
                        return NExprOpPrecedence.OR;
                    }
                }
                break;
            }
            case "|": {
                switch (type) {
                    case INFIX: {
                        return NExprOpPrecedence.PIPE;
                    }
                }
                break;
            }
            case "??": {
                if (type == null) {
                    type = NExprOpType.INFIX;
                }
                switch (type) {
                    case INFIX: {
                        return NExprOpPrecedence.COALESCE;
                    }
                }
                break;
            }
        }
        throw new IllegalArgumentException("unsupported operator " + name);
    }

    public static NOperatorAssociativity resolveOpDefaultAssociativity(String name, NExprOpType type, NOperatorAssociativity associativity) {
        NAssert.requireNamedNonNull(name, "name");
        NAssert.requireNamedNonNull(type, "type");
        if (associativity != null) {
            return associativity;
        }
        switch (name) {
            case "+":
            case "-":
            case "*":
            case "/":
            case "%":
            case ".":
            case "?.":
            case "<<":
            case ">>":
            case ">>>":
            case "<<<":
            case "<":
            case ">":
            case "<=":
            case ">=":
            case "=":
            case "==":
            case "!=":
            case "<>":
            case "&&":
            case "&":
            case "||":
            case "|":
            case "??": {
                switch (type) {
                    case INFIX: {
                        return NOperatorAssociativity.LEFT;
                    }
                }
                break;
            }
            case "(":
            case "()":
            case "[":
            case "[]":
            case "{":
            case "{}": {
                switch (type) {
                    case POSTFIX: {
                        return NOperatorAssociativity.LEFT;
                    }
                }
                break;
            }
        }
        throw new IllegalArgumentException("unsupported operator " + name);
    }
}

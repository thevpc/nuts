package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.util.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultOpNode implements NutsExprOpNode {
    private final String name;
    private final List<NutsExprNode> args;
    private final NutsExprOpType op;
    private final int precedence;

    public DefaultOpNode(String name, NutsExprOpType type, int precedence, List<NutsExprNode> args) {
        this.op = type;
        this.name = name;
        this.precedence = precedence;
        this.args = args;
    }


    @Override
    public NutsExprNode getOperand(int index) {
        if (index >= args.size()) {
            throw new IllegalArgumentException("Missing argument " + (index + 1) + " for " + name);
        }
        return args.get(index);
    }

    public List<NutsExprNode> getOperands() {
        return Collections.unmodifiableList(args);
    }

    @Override
    public NutsExprNodeType getType() {
        return NutsExprNodeType.OPERATOR;
    }

    @Override
    public List<NutsExprNode> getChildren() {
        return args;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        switch (name) {
            case "[]": {
                if (args.size() == 0) {
                    return name;
                }
                StringBuilder sb = new StringBuilder(EvalUtils.wrapPars(args.get(0)));
                sb.append("[");
                for (int i = 1; i < args.size(); i++) {
                    if (i > 1) {
                        sb.append(",");
                    }
                    sb.append(EvalUtils.wrapPars(args.get(i)));
                }
                sb.append("]");
                return sb.toString();
            }
            case "()": {
                if (args.size() == 0) {
                    return name;
                }
                StringBuilder sb = new StringBuilder(EvalUtils.wrapPars(args.get(0)));
                sb.append("(");
                for (int i = 1; i < args.size(); i++) {
                    if (i > 1) {
                        sb.append(",");
                    }
                    sb.append(EvalUtils.wrapPars(args.get(i)));
                }
                sb.append(")");
                return sb.toString();
            }
            case "{}": {
                if (args.size() == 0) {
                    return name;
                }
                StringBuilder sb = new StringBuilder(EvalUtils.wrapPars(args.get(0)));
                sb.append("{");
                for (int i = 1; i < args.size(); i++) {
                    if (i > 1) {
                        sb.append(",");
                    }
                    sb.append(EvalUtils.wrapPars(args.get(i)));
                }
                sb.append("}");
                return sb.toString();
            }
        }
        switch (op) {
            case PREFIX: {
                return name + " " + EvalUtils.wrapPars(args.get(0));
            }
            case POSTFIX: {
                return EvalUtils.wrapPars(args.get(0)) + name;
            }
            case INFIX: {
                if (args.size() == 2) {
                    return EvalUtils.wrapPars(args.get(0)) + " " + name + " " + EvalUtils.wrapPars(args.get(1));
                }
                if (args.size() > 2) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < args.size(); i++) {
                        if (i > 0) {
                            sb.append(" ").append(name).append(" ");
                        }
                        sb.append(EvalUtils.wrapPars(args.get(i)));
                    }
                    return sb.toString();
                }
            }
        }
        return name + "(" +
                args.stream().map(Object::toString).collect(Collectors.joining(",")) +
                ')';
    }

    @Override
    public NutsOptional<Object> eval(NutsExprDeclarations context) {
        try {
            return context.evalOperator(getName(), op, args.toArray(new NutsExprNode[0]));
        } catch (Exception ex) {
            return NutsOptional.ofError(x -> NutsMessage.ofCstyle("error %s ", ex));
        }
    }
}

package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.runtime.standalone.dependency.util.NComplexExpressionString;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultOpNode implements NExprOpNode {
    private final String name;
    private final String uniformName;
    private final List<NExprNode> args;
    private final NExprOpType opType;
    private final int precedence;

    public DefaultOpNode(String name, String uniformName, NExprOpType type, int precedence, List<NExprNode> args) {
        this.opType = type;
        this.name = name;
        this.uniformName = uniformName;
        this.precedence = precedence;
        this.args = args;
    }


    @Override
    public NExprNode getOperand(int index) {
        if (index >= args.size()) {
            throw new IllegalArgumentException("Missing argument " + (index + 1) + " for " + name);
        }
        return args.get(index);
    }

    public List<NExprNode> getOperands() {
        return Collections.unmodifiableList(args);
    }

    @Override
    public NExprNodeType getType() {
        return NExprNodeType.OPERATOR;
    }

    @Override
    public List<NExprNode> getChildren() {
        return args;
    }

    public String getName() {
        return name;
    }

    public String getUniformName() {
        return uniformName;
    }

    private String toParString(String start, String end) {
        int size = args.size();
        if (size == 0) {
            return start+end;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(start);
        sb.append(EvalUtils.wrapPars(args.get(0)));
        for (int i = 1; i < size; i++) {
            sb.append(",");
            sb.append(EvalUtils.wrapPars(args.get(i)));
        }
        sb.append(end);
        return sb.toString();
    }
    @Override
    public String toString() {
//        switch (name) {
//            case "[]": {
//                return toParString("[","]");
//            }
//            case "()": {
//                return toParString("(",")");
//            }
//            case "{}": {
//                return toParString("{","}");
//            }
//        }
        switch (opType) {
            case PREFIX: {
                switch (name) {
                    case "[": {
                        return toParString("[","]");
                    }
                    case "(": {
                        return toParString("(",")");
                    }
                    case "{": {
                        return toParString("{","}");
                    }
                    case "<": {
                        return toParString("<",">");
                    }
                }
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
                args.stream().map(NComplexExpressionString::toString).collect(Collectors.joining(",")) +
                ')';
    }

    @Override
    public NOptional<Object> eval(NExprDeclarations context) {
        switch (name) {
            case "()":
            case "(":
            {
                if(getChildren().size()==1){
                    return getChildren().get(0).eval(context);
                }
                break;
            }
        }
        try {
            return context.evalOperator(getName(), opType,
                    args.stream().map(context::nodeAsValue).toArray(NExprNodeValue[]::new)
                    );
        } catch (Exception ex) {
            return NOptional.ofError(() -> NMsg.ofC("error : %s", ex));
        }
    }
}

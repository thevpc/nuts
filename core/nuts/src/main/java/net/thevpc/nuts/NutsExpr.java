package net.thevpc.nuts;

import net.thevpc.nuts.spi.NutsComponent;

public interface NutsExpr extends NutsComponent {
    static NutsExpr of(NutsSession session){
        return session.extensions().createSupported(NutsExpr.class, true, null);
    }

    Node parse(String expression);

    NutsSession getSession();

    Var getVar(String varName);



    void setFunction(String name, Fct fct);

    void unsetFunction(String name);

    Fct getFunction(String fctName);

    String[] getFunctionNames();

    Object evalFunction(String fctName, Object... args);

    void setOperator(String name, OpType type, int precedence, boolean rightAssociative, Fct fct);

    Op getOperator(String fctName, OpType type);

    void unsetOperator(String name, OpType type);

    String[] getOperatorNames(OpType type);

    void setVar(String name, Var fct);

    Object evalVar(String fctName);

    NutsExpr newChild();

    Object evalNode(Node node);


    enum NodeType implements NutsEnum {
        FUNCTION,
        OPERATOR,
        VARIABLE,
        LITERAL;
        private final String id;

        NodeType() {
            this.id = name().toLowerCase().replace('_', '-');
        }

        public static NodeType parse(String value, NutsSession session) {
            return parse(value, null, session);
        }

        public static NodeType parse(String value, NodeType emptyValue, NutsSession session) {
            NodeType v = parseLenient(value, emptyValue, null);
            if (v == null) {
                if (!NutsBlankable.isBlank(value)) {
                    throw new NutsParseEnumException(session, value, NodeType.class);
                }
            }
            return v;
        }

        public static NodeType parseLenient(String value) {
            return parseLenient(value, null);
        }

        public static NodeType parseLenient(String value, NodeType emptyOrErrorValue) {
            return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
        }

        public static NodeType parseLenient(String value, NodeType emptyValue, NodeType errorValue) {
            if (value == null) {
                value = "";
            } else {
                value = value.toUpperCase().trim().replace('-', '_');
            }
            if (value.isEmpty()) {
                return emptyValue;
            }
            switch (value) {
                case "VAR":
                case "VARIABLE":
                    return VARIABLE;
                case "FCT":
                case "FUN":
                case "FUNCTION":
                    return FUNCTION;
                case "OP":
                case "OPERATOR":
                    return OPERATOR;
                case "LIT":
                case "LITERAL":
                    return LITERAL;
            }
            try {
                return NodeType.valueOf(value.toUpperCase());
            } catch (Exception notFound) {
                return errorValue;
            }
        }

        @Override
        public String id() {
            return id;
        }
    }

    enum OpType implements NutsEnum {
        INFIX,
        PREFIX,
        POSTFIX;
        private final String id;

        OpType() {
            this.id = name().toLowerCase().replace('_', '-');
        }

        public static OpType parse(String value, NutsSession session) {
            return parse(value, null, session);
        }

        public static OpType parse(String value, OpType emptyValue, NutsSession session) {
            OpType v = parseLenient(value, emptyValue, null);
            if (v == null) {
                if (!NutsBlankable.isBlank(value)) {
                    throw new NutsParseEnumException(session, value, OpType.class);
                }
            }
            return v;
        }

        public static OpType parseLenient(String value) {
            return parseLenient(value, null);
        }

        public static OpType parseLenient(String value, OpType emptyOrErrorValue) {
            return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
        }

        public static OpType parseLenient(String value, OpType emptyValue, OpType errorValue) {
            if (value == null) {
                value = "";
            } else {
                value = value.toUpperCase().trim().replace('-', '_');
            }
            if (value.isEmpty()) {
                return emptyValue;
            }
            switch (value) {
                case "INFIX":
                    return INFIX;
                case "POSTFIX_OPERATOR":
                case "POSTFIX_OP":
                case "POSTFIX":
                    return POSTFIX;
                case "PREFIX_OPERATOR":
                case "PREFIX_OP":
                case "PREFIX":
                    return PREFIX;
            }
            try {
                return OpType.valueOf(value.toUpperCase());
            } catch (Exception notFound) {
                return errorValue;
            }
        }

        @Override
        public String id() {
            return id;
        }
    }

    interface Node {
        Object eval(NutsExpr context);

        NodeType getType();

        Node[] getChildren();

        String getName();


    }

    interface Fct {
        Object eval(String name, Node[] args, NutsExpr context);
    }

    interface Op {
        boolean isLeftAssociative();

        boolean isRightAssociative();

        String getName();

        OpType getType();

        int getPrecedence();

        Fct getFct();
    }

    interface Var {
        Object get(String name, NutsExpr context);

        void set(String name, Object value, NutsExpr context);
    }

}

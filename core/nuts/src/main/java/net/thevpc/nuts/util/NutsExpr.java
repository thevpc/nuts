/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsEnum;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;

import java.util.List;

/**
 * Simple Expression Parser Module used in multiple syb-systems of nuts (such as search)
 */
public interface NutsExpr extends NutsComponent {
    static NutsExpr of(NutsSession session) {
        return session.extensions().createSupported(NutsExpr.class, true, null);
    }

    Node parse(String expression);

    NutsSession getSession();

    Var getVar(String varName);


    void setFunction(String name, Fct fct);

    void unsetFunction(String name);

    Fct getFunction(String fctName);

    List<String> getFunctionNames();

    Object evalFunction(String fctName, Object... args);

    void setOperator(String name, OpType type, int precedence, boolean rightAssociative, Fct fct);

    Op getOperator(String fctName, OpType type);

    void unsetOperator(String name, OpType type);

    List<String> getOperatorNames(OpType type);

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


        public static NutsOptional<NodeType> parse(String value) {
            return NutsApiUtils.parse(value, NodeType.class,s->{
                switch (s.toUpperCase()) {
                    case "VAR":
                    case "VARIABLE":
                        return NutsOptional.of(VARIABLE);
                    case "FCT":
                    case "FUN":
                    case "FUNCTION":
                        return NutsOptional.of(FUNCTION);
                    case "OP":
                    case "OPERATOR":
                        return NutsOptional.of(OPERATOR);
                    case "LIT":
                    case "LITERAL":
                        return NutsOptional.of(LITERAL);
                }
                return null;
            });
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

        public static NutsOptional<OpType> parse(String value) {
            return NutsApiUtils.parse(value, OpType.class,s->{
                switch (s.toUpperCase()) {
                    case "INFIX":
                        return NutsOptional.of(INFIX);
                    case "POSTFIX_OPERATOR":
                    case "POSTFIX_OP":
                    case "POSTFIX":
                        return NutsOptional.of(POSTFIX);
                    case "PREFIX_OPERATOR":
                    case "PREFIX_OP":
                    case "PREFIX":
                        return NutsOptional.of(PREFIX);
                }
                return null;
            });
        }

        @Override
        public String id() {
            return id;
        }
    }

    interface Node {
        Object eval(NutsExpr context);

        NodeType getType();

        List<Node> getChildren();

        String getName();


    }

    interface Fct {
        Object eval(String name, List<Node> args, NutsExpr context);
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

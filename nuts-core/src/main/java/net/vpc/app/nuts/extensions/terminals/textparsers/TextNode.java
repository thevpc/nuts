/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.terminals.textparsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author vpc
 */
public class TextNode {

    private String type;
    private String value;
    private List<TextNode> nodes = new ArrayList<>();

    public static TextNode PHRASE(String s) {
        return new TextNode("phrase", unbox(0, s));
    }

    private static String unbox(int prefix, String str) {
        char[] chars = str.substring(prefix, str.length() - prefix).toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '\\') {
                i++;
                c = chars[i];
                sb.append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static TextNode ANTI_CHAR(String s) {
        return new TextNode("`", unbox(1, s));
    }

    public static TextNode ANTI_CHAR2(String s) {
        return new TextNode("``", unbox(2, s));
    }

    public static TextNode ANTI_CHAR3(String s) {
        return new TextNode("```", unbox(3, s));
    }

    public static TextNode ANTI_CHAR4(String s) {
        return new TextNode("````", unbox(4, s));
    }

//    public static TextNode STRING(int index, String s) {
//        return new TextNode("string" + (index <= 1 ? "" : String.valueOf(index)), s);
//    }
//
//    public static TextNode CHAR(int index, String s) {
//        return new TextNode("char" + (index <= 1 ? "" : String.valueOf(index)), s);
//    }
    public static TextNode TYPED(String type, TextNode n) {
        return new TextNode(type, n);
    }

    public static TextNode PAR(TextNode n) {
        String t = n.getType();
        String op = "(";
        if (t != null && t.startsWith(op) && t.length() < 8) {
            return new TextNode(t + op, n.nodes.toArray(new TextNode[n.nodes.size()]));
        }
        return new TextNode(op, n);
    }

    public static TextNode ACC(TextNode n) {
        String t = n.getType();
        String op = "{";
        if (t != null && t.startsWith(op) && t.length() < 8) {
            return new TextNode(t + op, n.nodes.toArray(new TextNode[n.nodes.size()]));
        }
        return new TextNode(op, n);
    }

    public static TextNode BRAKETS(TextNode n) {
        String t = n.getType();
        String op = "[";
        if (t != null && t.startsWith(op) && t.length() < 8) {
            return new TextNode(t + op, n.nodes.toArray(new TextNode[n.nodes.size()]));
        }
        return new TextNode(op, n);
    }

    public static TextNode LIST(List<TextNode> nodes) {
        return LIST(nodes.toArray(new TextNode[nodes.size()]));
    }

    public static TextNode LIST(TextNode... nodes) {
        List<TextNode> all = new ArrayList<>();
        for (TextNode node : nodes) {
            if ("list".equals(node.getType())) {
                all.addAll(node.getNodes());
            } else {
                all.add(node);
            }
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new TextNode("list", all.toArray(new TextNode[all.size()]));
    }

//    public static TextNode LIST(List<TextNode> nodes) {
//        if (nodes.size() == 1) {
//            return nodes.get(0);
//        }
//        return new TextNode("list", nodes.toArray(new TextNode[nodes.size()]));
//    }
    public TextNode(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public TextNode(String type, TextNode... values) {
        this.type = type;
        nodes = new ArrayList<>(Arrays.asList(values));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<TextNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<TextNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        if ("list".equals(type)) {
            String s = nodes.toString();
            return s.substring(1, s.length() - 1);
        }
        if ("(".equals(type)) {
            String s = nodes.toString();
            return "(" + s.substring(1, s.length() - 1) + ")";
        }
        if ("[".equals(type)) {
            String s = nodes.toString();
            return "[" + s.substring(1, s.length() - 1) + "]";
        }
        if ("{".equals(type)) {
            String s = nodes.toString();
            return "[" + s.substring(1, s.length() - 1) + "}";
        }
        if ("phrase".equals(type)) {
            return value;
        }
        if ("string".equals(type)) {
            return value;//"\"" + value + "\"";
        }
        if ("string2".equals(type)) {
            return value;//"\"\"" + value + "\"\"";
        }
        if ("string3".equals(type)) {
            return value;//"\"\"\"" + value + "\"\"\"";
        }
        if ("char".equals(type)) {
            return value;//"\'" + value + "\'";
        }
        if ("char2".equals(type)) {
            return value;//"\'\'" + value + "\'\'";
        }
        if ("char3".equals(type)) {
            return value;//"\'\'\'" + value + "\'\'\'";
        }
        StringBuilder s = new StringBuilder("TextNode(" + "type=" + type);
        if (value != null) {
            s.append(", ").append(value);
        } else {
            s.append(", ").append(nodes);
        }
        return s.toString();
    }

}

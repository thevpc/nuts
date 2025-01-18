package net.thevpc.nuts.lib.doc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HtmlBuffer {
    private StringBuilder buffer = new StringBuilder();

    public HtmlBuffer tagPlain(String tag, String value) {
        buffer.append("<").append(tag).append(">")
                .append(value).append("</").append(tag).append(">");
        return this;
    }

    public HtmlBuffer newLine() {
        buffer.append("\n");
        return this;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    public static class Node {
    }

    public static class Attr {
        String name;
        String value;
        public Attr(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    public static class Plain extends Node {
        private String value;

        public Plain(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return (value==null?"":value.toString());
        }
    }

    public static class TagList extends Node {
        Node[] all;

        public TagList(List<Node>  all) {
            this(all.toArray(new Node[0]));
        }

        public TagList(Node... all) {
            this.all = all;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Node node : all) {
                sb.append(node);
                sb.append("\n");
            }
            return sb.toString();
        }
    }
    public static class Tag extends Node {
        String name;
        List<Attr> attrs=new ArrayList<>();
        List<Node> body=new ArrayList<>();
        boolean noEnd;
        public Tag(String name) {
            this.name=name;
        }

        public boolean isNoEnd() {
            return noEnd;
        }

        public Tag setNoEnd(boolean noEnd) {
            this.noEnd = noEnd;
            return this;
        }

        public Tag attr(String name, String value) {
            attrs.add(new Attr(name, value));
            return this;
        }

        public Tag body(Node value) {
            body.add(value);
            return this;
        }

        public Tag body(String value) {
            body.add(new Plain(value));
            return this;
        }

        @Override
        public String toString() {
            StringBuilder sb=new StringBuilder();
            sb.append("<").append(name);
            for (Attr attr : attrs) {
                sb.append(" ").append(attr.name).append("=\"").append(attr.value).append("\"");
            }
            if(body.size()==0 && noEnd) {
                sb.append("/>");
            }else {
                sb.append(">");
                for (Node b : body) {
                    sb.append(b);
                }
                sb.append("</").append(name).append(">");
            }
            return sb.toString();
        }
    }
}

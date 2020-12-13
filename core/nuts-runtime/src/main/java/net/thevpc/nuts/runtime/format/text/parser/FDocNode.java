///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.thevpc.nuts.runtime.util.fprint.parser;
//
///**
// *
// * @author thevpc
// */
//public class FDocNode {
//    private boolean partial;
//
//    public boolean isPartial() {
//        return partial;
//    }
//
//    public FDocNode setPartial(boolean partial) {
//        this.partial = partial;
//        return this;
//    }
//
//    private FDocNode(boolean partial) {
//        this.partial=partial;
//    }
//
//    public static final class Plain extends FDocNode {
//
//        private String value;
//
//        public Plain(String value,boolean partial) {
//            super(partial);
//            this.value = value;
//        }
//
//        public String getValue() {
//            return value;
//        }
//
//        @Override
//        public String toString() {
//            return "Plain{" + value + '}';
//        }
//
//    }
//
//    public static final class Typed extends FDocNode {
//
//        private String start;
//        private String end;
//        private FDocNode node;
//
//        public Typed(String start, String end, FDocNode value,boolean partial) {
//            super(partial);
//            this.start = start;
//            this.end = end;
//            this.node = value;
//        }
//
//        public String getStart() {
//            return start;
//        }
//
//        public String getEnd() {
//            return end;
//        }
//
//        public FDocNode getNode() {
//            return node;
//        }
//
//        @Override
//        public String toString() {
//            return "Typed&" + start + "&" + end + "&{" + node + '}';
//        }
//
//    }
//
//    public static final class Title extends FDocNode {
//
//        private String start;
//        private FDocNode node;
//
//        public Title(String start, FDocNode value,boolean partial) {
//            super(partial);
//            this.start = start;
//            this.node = value;
//        }
//
//        public String getStyleCode() {
//            int u = start.indexOf(')');
//            return start.substring(0,u);
//        }
//
//        public String getStart() {
//            return start;
//        }
//
//        public FDocNode getNode() {
//            return node;
//        }
//
//        @Override
//        public String toString() {
//            return "Title&" + start + "&{" + node + '}';
//        }
//
//    }
//
//    public static final class Escaped extends FDocNode {
//
//        private String start;
//        private String end;
//        private String value;
//
//        public Escaped(String start, String end, String value,boolean partial) {
//            super(partial);
//            this.start = start;
//            this.end = end;
//            this.value = value;
//        }
//
//        public String getStart() {
//            return start;
//        }
//
//        public String getEnd() {
//            return end;
//        }
//
//        public String getValue() {
//            return value;
//        }
//
//        @Override
//        public String toString() {
//            return "Escaped&" + start + "&" + end + "&{" + value + '}';
//        }
//
//    }
//
//    public static final class List extends FDocNode {
//
//        private FDocNode[] values;
//
//        public List(FDocNode[] values,boolean partial) {
//            super(partial);
//            this.values = values;
//        }
//
//        public FDocNode[] getValues() {
//            return values;
//        }
//
//        @Override
//        public String toString() {
//            return "List{" + values + '}';
//        }
//
//    }
//
//}

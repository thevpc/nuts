/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.fprint.parser;

/**
 *
 * @author vpc
 */
public class FDocNode {

    private FDocNode() {
    }

    public static final class Plain extends FDocNode {

        private String value;

        public Plain(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Plain{" + value + '}';
        }

    }

    public static final class Typed extends FDocNode {

        private String start;
        private String end;
        private FDocNode node;

        public Typed(String start, String end, FDocNode value) {
            this.start = start;
            this.end = end;
            this.node = value;
        }

        public String getStart() {
            return start;
        }

        public String getEnd() {
            return end;
        }

        public FDocNode getNode() {
            return node;
        }

        @Override
        public String toString() {
            return "Typed&" + start + "&" + end + "&{" + node + '}';
        }

    }

    public static final class Escaped extends FDocNode {

        private String start;
        private String end;
        private String value;

        public Escaped(String start, String end, String value) {
            this.start = start;
            this.end = end;
            this.value = value;
        }

        public String getStart() {
            return start;
        }

        public String getEnd() {
            return end;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Escaped&" + start + "&" + end + "&{" + value + '}';
        }

    }

    public static final class List extends FDocNode {

        private FDocNode[] values;

        public List(FDocNode[] values) {
            this.values = values;
        }

        public FDocNode[] getValues() {
            return values;
        }

        @Override
        public String toString() {
            return "List{" + values + '}';
        }

    }

}

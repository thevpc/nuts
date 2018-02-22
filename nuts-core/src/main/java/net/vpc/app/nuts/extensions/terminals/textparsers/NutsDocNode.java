/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.terminals.textparsers;

/**
 *
 * @author vpc
 */
public class NutsDocNode {

    private NutsDocNode() {
    }

    public static final class Plain extends NutsDocNode {

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

    public static final class Typed extends NutsDocNode {

        private String start;
        private String end;
        private NutsDocNode node;

        public Typed(String start, String end, NutsDocNode value) {
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

        public NutsDocNode getNode() {
            return node;
        }

        @Override
        public String toString() {
            return "Typed&" + start + "&" + end + "&{" + node + '}';
        }

    }

    public static final class Escaped extends NutsDocNode {

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

    public static final class List extends NutsDocNode {

        private NutsDocNode[] values;

        public List(NutsDocNode[] values) {
            this.values = values;
        }

        public NutsDocNode[] getValues() {
            return values;
        }

        @Override
        public String toString() {
            return "List{" + values + '}';
        }

    }

}

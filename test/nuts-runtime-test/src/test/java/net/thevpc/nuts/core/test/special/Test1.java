package net.thevpc.nuts.core.test.special;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Test1 {
    static class Pair<A,B>{
        A first;
        B second;
        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }
    }

    static class Node{
        String name;
        List<Node> children=new ArrayList<>();

        public Node(String name) {
            this.name = name;
        }

        public Node(String name, List<Node> children) {
            this.name = name;
            this.children = children;
        }

        public List<Node> getChildren() {
            return children;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    public static Node create() {
        Node a=new Node("a");
        Node a2;
        a.getChildren().add(new Node("b"));
        a.getChildren().add(a2=new Node("c"));

        a2.getChildren().add(new Node("d"));
        a2.getChildren().add(a2=new Node("e"));
        return a;
    }

    public static void main(String[] args) {
        Stack<Pair<Node, Integer>> stack = new Stack<>();
        Node root=create();
        System.out.println(" >> push   "+root.name+" idx:"+(0));
        stack.push(new Pair<>(root, 0));

        while (!stack.isEmpty()) {
            Pair<Node, Integer> frame = stack.pop();
            Node node = frame.first;
            int idx = frame.second;
            System.out.println(" >> popped "+node.name+" idx:"+(idx));

            if (idx == 0) {
                // First time we see this node
                visit(node);
            }

            if (idx < node.getChildren().size()) {
                // Push current node back with incremented index
                System.out.println(" >> push "+node.name+" idx:"+(idx+1));
                stack.push(new Pair<>(node, idx + 1));
                // Push the next child with index 0
                System.out.println(" >> push "+node.getChildren().get(idx).name+" idx:"+(0));
                stack.push(new Pair<>(node.getChildren().get(idx), 0));
            }
        }
    }

    private static void visit(Node node) {
        System.out.println(node.toString());
    }
}

package net.thevpc.nuts.toolbox.nsh.jshell;

import net.thevpc.nuts.toolbox.nsh.nodes.JShellCommandNode;

public class JShellScript implements JShellCommandNode {
    private final JShellCommandNode root;

    public JShellScript(JShellCommandNode root) {
        this.root = root;
    }

    @Override
    public int eval(JShellContext context) {
        return root.eval(context);
    }

    public JShellCommandNode getRoot() {
        return root;
    }
}

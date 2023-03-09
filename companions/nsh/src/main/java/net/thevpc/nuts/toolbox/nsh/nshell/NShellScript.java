package net.thevpc.nuts.toolbox.nsh.nshell;

import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellCommandNode;

public class NShellScript implements NShellCommandNode {
    private final NShellCommandNode root;

    public NShellScript(NShellCommandNode root) {
        this.root = root;
    }

    @Override
    public int eval(NShellContext context) {
        return root.eval(context);
    }

    public NShellCommandNode getRoot() {
        return root;
    }
}

package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

public interface JShellCommandNode extends JShellNode {
    int eval(JShellContext context);
}

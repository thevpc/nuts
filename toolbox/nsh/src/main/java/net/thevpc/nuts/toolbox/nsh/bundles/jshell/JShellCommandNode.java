package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

public interface JShellCommandNode extends JShellNode {
    void eval(JShellFileContext context);
}

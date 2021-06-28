package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

public interface JShellArgumentNode extends JShellNode {
    String[] evalString(JShellFileContext context);
}

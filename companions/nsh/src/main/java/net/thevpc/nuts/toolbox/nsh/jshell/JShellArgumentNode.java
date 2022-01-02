package net.thevpc.nuts.toolbox.nsh.jshell;

public interface JShellArgumentNode extends JShellNode {
    String[] evalString(JShellContext context);
}

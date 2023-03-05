package net.thevpc.nuts.toolbox.nsh.nodes;

import net.thevpc.nuts.toolbox.nsh.jshell.JShellContext;

public interface JShellCommandNode extends JShellNode {
    int eval(JShellContext context);
}

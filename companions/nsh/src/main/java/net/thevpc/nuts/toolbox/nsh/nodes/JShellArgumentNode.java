package net.thevpc.nuts.toolbox.nsh.nodes;

import net.thevpc.nuts.toolbox.nsh.jshell.JShellContext;
import net.thevpc.nuts.toolbox.nsh.nodes.JShellNode;

public interface JShellArgumentNode extends JShellNode {
    String[] evalString(JShellContext context);
}

package net.thevpc.nuts.toolbox.nsh.nodes;

import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;

public interface NShellArgumentNode extends NShellNode {
    String[] evalString(NShellContext context);
}

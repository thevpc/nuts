package net.thevpc.nuts.toolbox.nsh.nodes;

import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;

public interface NShellCommandNode extends NShellNode {
    int eval(NShellContext context);
}

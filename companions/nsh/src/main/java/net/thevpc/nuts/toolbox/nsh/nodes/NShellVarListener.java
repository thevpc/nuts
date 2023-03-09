package net.thevpc.nuts.toolbox.nsh.nodes;

import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;

public interface NShellVarListener {
    default void varAdded(NShellVar nShellVar, NShellVariables vars, NShellContext context) {

    }

    default void varValueUpdated(NShellVar nShellVar, String oldValue, NShellVariables vars, NShellContext context) {

    }

    default void varExportUpdated(NShellVar nShellVar, boolean oldValue, NShellVariables vars, NShellContext context) {

    }

    default void varRemoved(NShellVar nShellVar, NShellVariables vars, NShellContext context) {

    }
}

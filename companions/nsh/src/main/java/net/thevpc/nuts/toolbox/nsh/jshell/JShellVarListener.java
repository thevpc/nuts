package net.thevpc.nuts.toolbox.nsh.jshell;

import net.thevpc.nuts.toolbox.nsh.nodes.JShellVar;
import net.thevpc.nuts.toolbox.nsh.nodes.JShellVariables;

public interface JShellVarListener {
    default void varAdded(JShellVar jShellVar, JShellVariables vars, JShellContext context) {

    }

    default void varValueUpdated(JShellVar jShellVar, String oldValue, JShellVariables vars, JShellContext context) {

    }

    default void varExportUpdated(JShellVar jShellVar, boolean oldValue, JShellVariables vars, JShellContext context) {

    }

    default void varRemoved(JShellVar jShellVar, JShellVariables vars, JShellContext context) {

    }
}

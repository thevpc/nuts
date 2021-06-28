package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

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

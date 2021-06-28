package net.thevpc.nuts.toolbox.ndiff.jar;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface DiffEvalContext {
    Predicate<String> getPathFilter();

    boolean isDefaultPathFilterEnabled();

    List<DiffCommand> getSupportedCommands();

    boolean isVerbose();

    Map<String, Object> getUserProperties();

    Object getSource();

    Object getTarget();

    Map<String, Object> getLocalVars();
}

package net.thevpc.nuts.toolbox.ndiff.jar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class DefaultDiffEvalContext implements DiffEvalContext {
    private Object source;
    private Object target;
    private Map<String, Object> userProperties = new HashMap<>();
    private Map<String, Object> localVars = new HashMap<>();
    private boolean verbose;
    private boolean defaultPathFilterEnabled = true;
    private List<DiffCommand> commands = new ArrayList<>();
    private Predicate<String> pathFilter;

    public DefaultDiffEvalContext() {
    }

    public DefaultDiffEvalContext(DiffEvalContext other) {
        this.setSource(other.getSource());
        this.setTarget(other.getTarget());
        this.setVerbose(other.isVerbose());
        this.setSupportedCommands(other.getSupportedCommands());
        this.setUserProperties(other.getUserProperties());
        this.setPathFilter(other.getPathFilter());
        this.setDefaultPathFilterEnabled(other.isDefaultPathFilterEnabled());
    }

    @Override
    public Predicate<String> getPathFilter() {
        return pathFilter;
    }

    public void setPathFilter(Predicate<String> pathFilter) {
        this.pathFilter = pathFilter;
    }

    @Override
    public boolean isDefaultPathFilterEnabled() {
        return defaultPathFilterEnabled;
    }

    public void setDefaultPathFilterEnabled(boolean defaultPathFilterEnabled) {
        this.defaultPathFilterEnabled = defaultPathFilterEnabled;
    }

    public void pathFilter(Predicate<String> pathFilter) {
        this.pathFilter = pathFilter;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setSupportedCommands(List<DiffCommand> commands) {
        this.commands.clear();
        this.commands.addAll(commands);
    }

    @Override
    public List<DiffCommand> getSupportedCommands() {
        return (List) commands;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    public void setUserProperties(Map<String, Object> userProperties) {
        this.userProperties = new HashMap<>();
        if (userProperties != null) {
            this.userProperties.putAll(userProperties);
        }
    }

    @Override
    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    @Override
    public Map<String, Object> getLocalVars() {
        return localVars;
    }

    public void setLocalVars(Map<String, Object> localVars) {
        this.localVars = localVars;
    }
}

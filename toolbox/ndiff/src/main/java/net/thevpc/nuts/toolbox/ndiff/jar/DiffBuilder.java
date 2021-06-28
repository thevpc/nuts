package net.thevpc.nuts.toolbox.ndiff.jar;

import net.thevpc.nuts.toolbox.ndiff.jar.commands.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class DiffBuilder {
    private Object source;
    private Object target;
    private String type;
    private boolean verbose;
    private boolean defaultPathFilterEnabled = true;
    private Predicate<String> pathFilter;
    private List<DiffCommand> commands = new ArrayList<>();
    private Map<String, Object> userProperties = new HashMap<>();

    public DiffBuilder(Object source, Object target) {
        this.source = source;
        this.target = target;
        commands.add(DiffCommandJavaClass.INSTANCE);
        commands.add(DiffCommandJavaManifest.INSTANCE);
        commands.add(DiffCommandJavaProperties.INSTANCE);
        commands.add(DiffCommandJar.INSTANCE);
        commands.add(DiffCommandZip.INSTANCE);
        commands.add(DiffCommandVar.INSTANCE);
    }

    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    public DiffBuilder setUserProperties(Map<String, Object> userProperties) {
        this.userProperties = new HashMap<>();
        if (userProperties != null) {
            for (Map.Entry<String, Object> e : userProperties.entrySet()) {
                if (e.getValue() != null) {
                    this.userProperties.put(e.getKey(), e.getValue());
                }
            }
        }
        return this;
    }

    public DiffBuilder userProperty(String key, Object value) {
        if (value != null) {
            this.userProperties.put(key, value);
        } else {
            this.userProperties.remove(key);
        }
        return this;
    }

    public DiffBuilder userProperties(Map<String, Object> userProperties) {
        return appendUserProperties(userProperties);
    }

    public DiffBuilder appendUserProperties(Map<String, Object> userProperties) {
        if (userProperties != null) {
            for (Map.Entry<String, Object> e : userProperties.entrySet()) {
                userProperty(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    public boolean isDefaultPathFilterEnabled() {
        return defaultPathFilterEnabled;
    }

    public DiffBuilder setDefaultPathFilterEnabled(boolean defaultPathFilterEnabled) {
        this.defaultPathFilterEnabled = defaultPathFilterEnabled;
        return this;
    }

    public DiffBuilder verbose() {
        return verbose(true);
    }

    public DiffBuilder verbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public DiffBuilder as(String type) {
        this.type = type;
        return this;
    }

    public Predicate<String> getPathFilter() {
        return pathFilter;
    }

    public DiffBuilder setPathFilter(Predicate<String> pathFilter) {
        this.pathFilter = pathFilter;
        return this;
    }

    public DiffBuilder pathFilter(Predicate<String> pathFilter) {
        this.pathFilter = pathFilter;
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public DiffResult eval() {
        DefaultDiffEvalContext c = new DefaultDiffEvalContext();
        c.setSource(source);
        c.setTarget(target);
        c.setSupportedCommands(commands);
        c.setVerbose(isVerbose());
        c.setUserProperties(getUserProperties());
        c.setPathFilter(getPathFilter());
        c.setDefaultPathFilterEnabled(isDefaultPathFilterEnabled());
        int best = 0;
        DiffCommand bestCommand = null;
        for (DiffCommand command : commands) {
            if (type != null && command.getId().equals(type)) {
                bestCommand = command;
                break;
            } else {
                int a = 0;
                int b = 0;
                if ((a = command.acceptInput(source)) > 0 && (b = command.acceptInput(target)) > 0) {
                    if (a > best && b > best) {
                        bestCommand = command;
                        best = Math.min(a, b);
                    }
                }
            }
        }
        if (bestCommand != null) {
            return bestCommand.eval(c);
        }
        throw new IllegalArgumentException("Unable to build diff command");

    }
}

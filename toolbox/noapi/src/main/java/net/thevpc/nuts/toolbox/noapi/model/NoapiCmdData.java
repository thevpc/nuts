package net.thevpc.nuts.toolbox.noapi.model;

import net.thevpc.nuts.toolbox.noapi.NoapiMain;

import java.util.HashMap;
import java.util.Map;

public class NoapiCmdData {
    private String command;
    private String path;
    private String vars;
    private Map<String, String> varsMap = new HashMap<>();
    private String target;
    private boolean openAPI;
    private boolean keep;
    private String openAPIFormat;

    public String getCommand() {
        return command;
    }

    public NoapiCmdData setCommand(String command) {
        this.command = command;
        return this;
    }

    public String getPath() {
        return path;
    }

    public NoapiCmdData setPath(String path) {
        this.path = path;
        return this;
    }

    public String getVars() {
        return vars;
    }

    public NoapiCmdData setVars(String vars) {
        this.vars = vars;
        return this;
    }

    public Map<String, String> getVarsMap() {
        return varsMap;
    }

    public NoapiCmdData setVarsMap(Map<String, String> varsMap) {
        this.varsMap = varsMap;
        return this;
    }

    public String getTarget() {
        return target;
    }

    public NoapiCmdData setTarget(String target) {
        this.target = target;
        return this;
    }

    public boolean isOpenAPI() {
        return openAPI;
    }

    public NoapiCmdData setOpenAPI(boolean openAPI) {
        this.openAPI = openAPI;
        return this;
    }

    public boolean isKeep() {
        return keep;
    }

    public NoapiCmdData setKeep(boolean keep) {
        this.keep = keep;
        return this;
    }

    public String getOpenAPIFormat() {
        return openAPIFormat;
    }

    public NoapiCmdData setOpenAPIFormat(String openAPIFormat) {
        this.openAPIFormat = openAPIFormat;
        return this;
    }
}

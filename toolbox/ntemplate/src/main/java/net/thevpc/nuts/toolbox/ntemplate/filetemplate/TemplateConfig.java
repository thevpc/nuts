/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ntemplate.filetemplate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author thevpc
 */
public class TemplateConfig {

    private List<String> paths = new ArrayList<String>();
    private String targetFolder;
    private List<String> initScripts = new ArrayList<>();
    private String scriptType;
    private String projectPath;
    private Predicate<Path> pathFilter;

    public TemplateConfig addSource(String script) {
        paths.add(script);
        return this;
    }
    
    public TemplateConfig addInitScript(String script) {
        initScripts.add(script);
        return this;
    }

    public TemplateConfig setScriptType(String value) {
        this.scriptType = value;
        return this;
    }

    public TemplateConfig setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
        return this;
    }

    public TemplateConfig setProjectPath(String projectPath) {
        this.projectPath = projectPath;
        return this;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public String getScriptType() {
        return scriptType;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public List<String> getPaths() {
        return paths;
    }

    public List<String> getInitScripts() {
        return initScripts;
    }

    public Predicate<Path> getPathFilter() {
        return pathFilter;
    }

    public TemplateConfig setPathFilter(Predicate<Path> pathFilter) {
        this.pathFilter = pathFilter;
        return this;
    }
    
    
}

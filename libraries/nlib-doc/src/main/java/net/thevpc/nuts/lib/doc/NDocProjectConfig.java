/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.io.NPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author thevpc
 */
public class NDocProjectConfig implements NCmdLineConfigurable ,Cloneable{

    private String targetFolder;
    private String scriptType;
    private String projectPath;
    private Predicate<NPath> pathFilter;
    private String contextName;
    private boolean clean;
    private String javadocTarget;
    private String javadocBackend;

    private List<String> javaSourcePaths = new ArrayList<>();
    private List<String> javaPackages = new ArrayList<>();
    private List<String> sourcePaths = new ArrayList<String>();
    private List<String> resourcePaths = new ArrayList<String>();
    private List<String> initScripts = new ArrayList<>();
    private Map<String, Object> vars;

    public List<String> getJavaSourcePaths() {
        return javaSourcePaths;
    }

    public List<String> getJavaPackages() {
        return javaPackages;
    }

    public String getJavadocTarget() {
        return javadocTarget;
    }

    public String getJavadocBackend() {
        return javadocBackend;
    }

    public void setJavadocTarget(String javadocTarget) {
        this.javadocTarget = javadocTarget;
    }

    public void setJavadocBackend(String javadocBackend) {
        this.javadocBackend = javadocBackend;
    }

    public boolean isClean() {
        return clean;
    }

    public NDocProjectConfig setClean(boolean clean) {
        this.clean = clean;
        return this;
    }

    public Map<String, Object> getVars() {
        return vars;
    }

    public NDocProjectConfig setVars(Map<String, Object> vars) {
        this.vars = vars;
        return this;
    }

    public String getContextName() {
        return contextName;
    }

    public NDocProjectConfig setContextName(String contextName) {
        this.contextName = contextName;
        return this;
    }

    public NDocProjectConfig addSource(String script) {
        sourcePaths.add(script);
        return this;
    }

    public NDocProjectConfig addResourceSource(String script) {
        resourcePaths.add(script);
        return this;
    }

    public NDocProjectConfig addInitScript(String script) {
        initScripts.add(script);
        return this;
    }

    public NDocProjectConfig setScriptType(String value) {
        this.scriptType = value;
        return this;
    }

    public NDocProjectConfig setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
        return this;
    }

    public NDocProjectConfig setProjectPath(String projectPath) {
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

    public List<String> getSourcePaths() {
        return sourcePaths;
    }

    public List<String> getResourcePaths() {
        return resourcePaths;
    }

    public List<String> getInitScripts() {
        return initScripts;
    }

    public Predicate<NPath> getPathFilter() {
        return pathFilter;
    }

    public void setJavaSourcePaths(List<String> javaSourcePaths) {
        this.javaSourcePaths = javaSourcePaths;
    }

    public void setJavaPackages(List<String> javaPackages) {
        this.javaPackages = javaPackages;
    }

    public void setSourcePaths(List<String> sourcePaths) {
        this.sourcePaths = sourcePaths;
    }

    public void setResourcePaths(List<String> resourcePaths) {
        this.resourcePaths = resourcePaths;
    }

    public void setInitScripts(List<String> initScripts) {
        this.initScripts = initScripts;
    }

    public NDocProjectConfig setPathFilter(Predicate<NPath> pathFilter) {
        this.pathFilter = pathFilter;
        return this;
    }

    public NDocProjectConfig copy(){
        return clone();
    }

    @Override
    protected NDocProjectConfig clone()  {
        try {
            NDocProjectConfig clone = (NDocProjectConfig) super.clone();
            if(clone.javaSourcePaths!=null){
                clone.javaSourcePaths=new ArrayList<>(clone.javaSourcePaths);
            }
            if(clone.javaPackages!=null){
                clone.javaPackages=new ArrayList<>(clone.javaPackages);
            }
            if(clone.sourcePaths!=null){
                clone.sourcePaths=new ArrayList<>(clone.sourcePaths);
            }
            if(clone.resourcePaths!=null){
                clone.resourcePaths=new ArrayList<>(clone.resourcePaths);
            }
            if(clone.initScripts!=null){
                clone.initScripts=new ArrayList<>(clone.initScripts);
            }
            if(clone.vars!=null){
                clone.vars=new HashMap<>(clone.vars);
            }

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg option = cmdLine.peek().get();
        switch (option.key()) {
            case "-i":
            case "--init": {
                cmdLine.withNextEntry((v, r) -> NDocProjectConfig.this.addInitScript(v));
                return true;
            }
            case "--clean": {
                cmdLine.withNextFlag((v, r) -> NDocProjectConfig.this.setClean(v));
                return true;
            }
            case "--script": {
                cmdLine.withNextEntry((v, r) -> NDocProjectConfig.this.setScriptType(v));
                return true;
            }
            case "-t":
            case "--to": {
                cmdLine.withNextEntry((v, r) -> NDocProjectConfig.this.setTargetFolder(v));
                return true;
            }
            case "-s":
            case "--src": {
                cmdLine.withNextEntry((v, r) -> NDocProjectConfig.this.addSource(v));
                return true;
            }
            case "-p":
            case "--project": {
                cmdLine.withNextEntry((v, r) -> NDocProjectConfig.this.setProjectPath(v));
                return true;
            }
            case "-r":
            case "--resource": {
                cmdLine.withNextEntry((v, r) -> NDocProjectConfig.this.addResourceSource(v));
                return true;
            }
            ///////////////////////////

            case "--java-source": {
                cmdLine.withNextEntry((v, r) -> javaSourcePaths.add(v));
                return true;
            }
            case "--javadoc-target": {
                cmdLine.withNextEntry((v, r) -> javadocTarget = v);
                return true;
            }
            case "--java-package": {
                cmdLine.withNextEntry((v, r) -> javaPackages.add(v));
                return true;
            }
            case "--javadoc-backend": {
                cmdLine.withNextEntry((v, r) -> javadocBackend = v);
                return true;
            }
        }
        return false;
    }
}

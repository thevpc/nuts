package net.thevpc.nuts.toolbox.ntemplate.project;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.TemplateConfig;

import java.util.Map;

public class NTemplateProject {
    private TemplateConfig config;
    private NFileTemplater fileTemplater;

    public NTemplateProject(TemplateConfig config) {
        this.config=config;
        this.fileTemplater = new NFileTemplater();
    }

    public void setVars(Map<String,Object> vars) {
        getFileTemplater().setVars(vars);
    }

    public void setVar(String name, Object value) {
        getFileTemplater().setVar(name,value);
    }

    public NFileTemplater getFileTemplater() {
        return fileTemplater;
    }

    public void run(){
        fileTemplater.processProject(config);
    }
}

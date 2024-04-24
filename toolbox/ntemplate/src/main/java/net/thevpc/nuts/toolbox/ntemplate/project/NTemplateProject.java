package net.thevpc.nuts.toolbox.ntemplate.project;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.TemplateConfig;

import java.util.Map;

public class NTemplateProject {
    private TemplateConfig config;
    private NSession session;
    private NFileTemplater fileTemplater;

    public NTemplateProject(TemplateConfig config, NSession session) {
        this.config=config;
        this.session=session;
        this.fileTemplater = new NFileTemplater(session);
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

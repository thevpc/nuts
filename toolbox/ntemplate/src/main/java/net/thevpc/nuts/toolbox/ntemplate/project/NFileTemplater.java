package net.thevpc.nuts.toolbox.ntemplate.project;

import net.thevpc.nuts.toolbox.ntemplate.filetemplate.FileTemplater;

import java.nio.file.Path;

public class NFileTemplater extends FileTemplater {
    public NFileTemplater() {
        super();
        this.setDefaultExecutor("text/ntemplate-nsh-project", new NshEvaluator(this));
        setProjectFileName("project.nsh");
    }

    public void executeProjectFile(Path path, String mimeTypesString) {
        executeRegularFile(path, "text/ntemplate-nsh-project");
    }
}

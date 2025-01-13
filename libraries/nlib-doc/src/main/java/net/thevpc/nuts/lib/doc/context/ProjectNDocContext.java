package net.thevpc.nuts.lib.doc.context;

import net.thevpc.nuts.lib.doc.executor.nsh.NshEvaluator;

import java.nio.file.Path;

public class ProjectNDocContext extends NDocContext {
    public ProjectNDocContext() {
        super();
        this.getExecutorManager().setDefaultExecutor("text/ndoc-nsh-project", new NshEvaluator(this));
        setProjectFileName("project.nsh");
    }

    public void executeProjectFile(Path path, String mimeTypesString) {
        getExecutorManager().executeRegularFile(path, "text/ndoc-nsh-project");
    }
}

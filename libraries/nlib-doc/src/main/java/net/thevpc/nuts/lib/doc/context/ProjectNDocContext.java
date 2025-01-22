package net.thevpc.nuts.lib.doc.context;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.executor.nsh.NshEvaluator;

public class ProjectNDocContext extends NDocContext {
    public ProjectNDocContext() {
        super();
        this.getExecutorManager().setDefaultExecutor("text/ndoc-nsh-project", new NshEvaluator(this));
        setProjectFileName("project.nsh");
    }

    public void executeProjectFile(NPath path, String mimeTypesString) {
        getExecutorManager().executeRegularFile(path, "text/ndoc-nsh-project");
    }
}

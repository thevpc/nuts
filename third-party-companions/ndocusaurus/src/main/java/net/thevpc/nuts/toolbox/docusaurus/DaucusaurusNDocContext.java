package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.lib.doc.context.NDocContext;

import java.nio.file.Path;

class DaucusaurusNDocContext extends NDocContext {
    public DaucusaurusNDocContext() {
        super();
        this.getExecutorManager().setDefaultExecutor("text/ndoc-nsh-project", new DocusaurusNshEvaluator(this));
        setProjectFileName("project.nsh");
    }

    public void executeProjectFile(Path path, String mimeTypesString) {
        getExecutorManager().executeRegularFile(path, "text/ndoc-nsh-project");
    }
}

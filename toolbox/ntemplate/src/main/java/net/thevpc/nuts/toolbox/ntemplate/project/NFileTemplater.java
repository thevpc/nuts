package net.thevpc.nuts.toolbox.ntemplate.project;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.FileTemplater;

import java.nio.file.Path;

public class NFileTemplater extends FileTemplater {
    public NFileTemplater(NSession session) {
        super(session);
        this.setDefaultExecutor("text/ntemplate-nsh-project", new NshEvaluator(session, this));
        setProjectFileName("project.nsh");
    }

    public void executeProjectFile(Path path, String mimeTypesString) {
        executeRegularFile(path, "text/ntemplate-nsh-project");
    }
}

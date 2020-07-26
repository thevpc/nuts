package net.vpc.toolbox.worky.fileprocessors;

import net.vpc.common.textsource.*;
import net.vpc.common.textsource.log.JTextSourceLog;
import net.vpc.toolbox.worky.fileprocessors.nodes.ExprEvaluator;

public class TextProcessor extends AbstractContentProcessor {

    @Override
    public String processSource(JTextSource source, String workingDir,JTextSourceLog messages) {
        return FileProcessorUtils.processSource(source,workingDir,new ExprEvaluator(), messages);
    }


}

package net.vpc.toolbox.worky.fileprocessors;

import net.vpc.common.textsource.*;
import net.vpc.common.textsource.log.JTextSourceLog;
import net.vpc.toolbox.worky.fileprocessors.nodes.ExprEvaluator;
import net.vpc.toolbox.worky.fileprocessors.nodes.ExprNode;
import net.vpc.toolbox.worky.fileprocessors.nodes.ExprNodeParser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextProcessor extends AbstractContentProcessor {

    @Override
    public String processSource(JTextSource source, JTextSourceLog messages) {
        return FileProcessorUtils.processSource(source,new ExprEvaluator(), messages);
    }


}

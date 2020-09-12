package net.vpc.toolbox.worky.fileprocessors;

import net.vpc.common.textsource.JTextSource;
import net.vpc.common.textsource.JTextSourceFactory;
import net.vpc.common.textsource.JTextSourcePosition;
import net.vpc.common.textsource.log.JTextSourceLog;
import net.vpc.toolbox.worky.fileprocessors.nodes.ExprEvaluator;
import net.vpc.toolbox.worky.fileprocessors.nodes.ExprNode;
import net.vpc.toolbox.worky.fileprocessors.nodes.ExprNodeParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractContentProcessor implements ContentProcessor{
    public String processExpr(String content, JTextSource source, String workingDir,ExprEvaluator exprEvaluator, JTextSourceLog messages, JTextSourcePosition tracker) {
        content = content.trim();
        ExprNode p=new ExprNodeParser(content,messages,workingDir).parseDocument();
        if(p!=null){
            return String.valueOf(exprEvaluator.eval(p,messages,workingDir));
        }
        return "";
    }

    @Override
    public void processRegularFile(Path path, String workingDir, JTextSourceLog messages){
        if (Files.isRegularFile(path)) {
            if (path.toString().endsWith(".pre")) {
                Path r = Paths.get(path.toString().substring(0, path.toString().length() - 4));
                try {
                    JTextSource source = JTextSourceFactory.fromFile(path);
                    if(source!=null) {
                        Path parent = path.getParent();
                        if(parent==null){
                            parent=Paths.get(System.getProperty("user.dir"));
                        }
                        String outputContent = new TextProcessor().processSource(source,parent.toString(), messages);
                        Files.write(r, outputContent.getBytes());
                    }else{
                        messages.error("X000",null,"invalid source :"+path,null);
                    }
                } catch (IOException e) {
                    messages.error("X000",null,"unexpected error :"+e.toString(),null);
                }
            }
        }
    }
}

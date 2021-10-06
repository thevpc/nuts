package net.thevpc.nuts.toolbox.ntemplate.filetemplate.eval;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.ExprEvaluator;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.FileTemplater;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.MimeTypeConstants;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.FileProcessorUtils;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FtexEvaluator implements ExprEvaluator {

    private Map<String, ExprEvalFct> functions = new HashMap<>();

    public FtexEvaluator() {
        addFunction(new AbstractExprEvalEct(";") {
            @Override
            public Object evalFunction(ExprNode[] args, FileTemplater context) {
                Object a = "";
                for (ExprNode arg : args) {
                    a = evalNode(arg, context);
                }
                return a;
            }
        });
        addFunction(new AbstractExprEvalEct("get") {
            @Override
            public Object evalFunction(ExprNode[] args, FileTemplater context) {
                if (args.length != 1) {
                    throw new IllegalStateException(getName() + " : invalid arguments count");
                }
                String varName = String.valueOf(evalNode(args[0], context));
                return context.getVarRequired(varName);
            }
        });
        addFunction(new AbstractExprEvalEct("set") {
            @Override
            public Object evalFunction(ExprNode[] args, FileTemplater context) {
                if (args.length != 2) {
                    throw new IllegalStateException(getName() + " : invalid arguments count");
                }
                String varName = String.valueOf(evalNode(args[0], context));
                Object newValue = evalNode(args[1], context);
                context.getLog().debug("eval", varName + "=" + StringUtils.toLiteralString(newValue));
                context.setVar(varName, newValue);
                return "";
            }
        });
        addFunction(new AbstractExprEvalEct("exec") {
            @Override
            public Object evalFunction(ExprNode[] args, FileTemplater context) {
                if (args.length != 1) {
                    throw new IllegalStateException(getName() + " : invalid arguments count");
                }
                String pathString = (String) evalNode(args[0], context);
                context.getLog().debug("eval", getName() + "(" + StringUtils.toLiteralString(pathString) + ")");
                return context.executeRegularFile(Paths.get(pathString), null);
            }
        });
        addFunction(new AbstractExprEvalEct("println") {
            @Override
            public Object evalFunction(ExprNode[] args, FileTemplater context) {
                List<String> all = new ArrayList<>();
                for (ExprNode arg : args) {
                    all.add(String.valueOf(evalNode(arg, context)));
                }
                StringBuilder sb = new StringBuilder();
                if (!all.isEmpty()) {
                    if (all.size() == 1) {
                        sb.append(all.get(0)).append("\n");
                    } else {
                        sb.append(String.join(", ", all)).append("\n");
                    }
                }
                context.getLog().debug("eval", getName() + "(" + StringUtils.toLiteralString(sb.toString()) + ")");
                return sb.toString();
            }
        });
        addFunction(new AbstractExprEvalEct("print") {
            @Override
            public Object evalFunction(ExprNode[] args, FileTemplater context) {
                List<String> all = new ArrayList<>();
                for (ExprNode arg : args) {
                    all.add(String.valueOf(evalNode(arg, context)));
                }
                StringBuilder sb = new StringBuilder();
                if (!all.isEmpty()) {
                    if (all.size() == 1) {
                        sb.append(all.get(0));
                    } else {
                        sb.append(String.join(", ", all));
                    }
                }
                context.getLog().debug("eval", getName() + "(" + StringUtils.toLiteralString(sb.toString()) + ")");
                return sb.toString();
            }
        });
        addFunction(new AbstractExprEvalEct("string") {
            @Override
            public Object evalFunction(ExprNode[] args, FileTemplater context) {
                if (args.length != 1) {
                    throw new IllegalStateException(getName() + " : invalid arguments count");
                }
                String str = (String) evalNode(args[0], context);
                context.getLog().debug("eval", getName() + "(" + StringUtils.toLiteralString(str) + ")");
                return "\"" + StringUtils.escapeString(str) + "\"";
            }
        });
        addFunction(new AbstractExprEvalEct("processFile") {
            @Override
            public Object evalFunction(ExprNode[] args, FileTemplater context) {
                if (args.length != 1) {
                    throw new IllegalStateException(getName() + " : invalid arguments count");
                }
                String str = (String) evalNode(args[0], context);
                String path = FileProcessorUtils.toAbsolute(str, context.getWorkingDirRequired());
                Path opath = Paths.get(path);
                context.getLog().debug("eval", getName() + "(" + StringUtils.toLiteralString(opath) + ")");
                context.processRegularFile(opath, null);
                return "";
            }
        });
        addFunction(new AbstractExprEvalEct("loadFile") {
            @Override
            public Object evalFunction(ExprNode[] args, FileTemplater context) {
                if (args.length != 1) {
                    throw new IllegalStateException(getName() + " : invalid arguments count");
                }
                String str = (String) evalNode(args[0], context);
                context.getLog().debug("eval", getName() + "(" + StringUtils.toLiteralString(str) + ")");
                return FileProcessorUtils.loadString(
                        Paths.get(FileProcessorUtils.toAbsolute(str, context.getWorkingDirRequired()))
                );
            }
        });
        addFunction(new AbstractExprEvalEct("include") {
            @Override
            public Object evalFunction(ExprNode[] args, FileTemplater context) {
                if (args.length != 1) {
                    throw new IllegalStateException(getName() + " : invalid arguments count");
                }
                String str = (String) evalNode(args[0], context);
                String path = FileProcessorUtils.toAbsolute(str, context.getWorkingDirRequired());
                Path opath = Paths.get(path);
                context.getLog().debug("eval", getName() + "(" + StringUtils.toLiteralString(opath) + ")");
                try (InputStream in = Files.newInputStream(opath)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    context.processStream(in, out, context.getMimeTypeResolver().resolveMimetype(opath.toString()));
                    return out.toString();
                } catch (IOException io) {
                    throw new UncheckedIOException(io);
                }
            }
        });
    }

    @Override
    public Object eval(String content, FileTemplater context) {
        content = content.trim();
        ExprNode p = new ExprNodeParser(content, context).parseDocument();
        if (p != null) {
            return String.valueOf(evalNode(p, context));
        }
        return "";
    }

    protected Object evalFunction(String functionName, ExprNode[] args, FileTemplater context) {
        return getFunction(functionName).evalFunction(args, context);
    }

    public Object evalNode(ExprNode node, FileTemplater context) {
        switch (node.getClass().getSimpleName()) {
            case "ExprNodeLiteral": {
                ExprNodeLiteral n = (ExprNodeLiteral) node;
                switch (n.getType()) {
                    case "\"": {
                        return evalDoubleQuotesString((String) n.getValue(), context);
                    }
                    case "'": {
                        return evalSimpleQuotesString((String) n.getValue(), context);
                    }
                    case "`": {
                        return evalAntiQuotesString((String) n.getValue(), context);
                    }
                    case "$\"": {
                        return evalDoubleQuotesStringInterp((String) n.getValue(), context);
                    }
                    case "number": {
                        return evalDouble((Double) n.getValue(), context);
                    }
                    default: {
                        throw new IllegalArgumentException("Unsupported " + n.getType());
                    }
                }
            }

            case "ExprNodeVar": {
                return getFunction("get").evalFunction(
                        new ExprNode[]{new ExprNodeLiteral("\"", ((ExprNodeVar) node).getName())},
                        context);
            }
            case "ExprNodeFunction": {
                ExprNodeFunction f = ((ExprNodeFunction) node);
                String functionName = f.getName();
                ExprNode[] args = f.getArgs();
                return evalFunction(functionName, args, context);
            }

            default: {
                throw new IllegalStateException("Invalid statement " + node);
            }
        }
    }

    @Override
    public String toString() {
        return "Ftex";
    }

    private Object evalDouble(Double value, FileTemplater ctx) {
        return value;
    }

    private Object evalAntiQuotesString(String value, FileTemplater ctx) {
        NutsSession ws = ctx.getSession();
        return ws.exec().addCommand(
                ws.commandLine().parse(value).toStringArray()
        ).setDirectory(ctx.getWorkingDirRequired())
                .grabOutputString()
                .run()
                .getOutputString();
    }

    private Object evalSimpleQuotesString(String value, FileTemplater ctx) {
        return value;
    }

    private Object evalDoubleQuotesString(String value, FileTemplater ctx) {
        return value;
    }

    private Object evalDoubleQuotesStringInterp(String value, FileTemplater context) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        context.newChild().processStream(
                new ByteArrayInputStream(value.getBytes()),
                baos, MimeTypeConstants.PLACEHOLDER_DOLLARS);
        return baos.toString();
    }

    public FtexEvaluator removeFunction(String name) {
        functions.remove(name);
        return this;
    }

    public final FtexEvaluator addFunction(ExprEvalFct fct) {
        functions.put(fct.getName(), fct);
        return this;
    }

    public ExprEvalFct findFunction(String name) {
        return functions.get(name);
    }

    public ExprEvalFct getFunction(String name) {
        ExprEvalFct f = functions.get(name);
        if (f == null) {
            throw new NoSuchElementException("function not found " + name);
        }
        return f;
    }
}
